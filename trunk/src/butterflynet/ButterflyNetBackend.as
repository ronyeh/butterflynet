package butterflynet {

	import flash.events.*;
	import flash.net.*;
	import flash.display.Sprite;
	import mx.controls.TextArea;
	import ink.Ink;
	import ink.InkStroke;
	import ink.InkXMLParser;
	import ink.InkRawXMLParser;
	import flash.system.System;
	import flash.system.Security;
	import flash.utils.Timer;
	import flash.filesystem.File;
	import flash.display.Stage;
	import flash.display.StageDisplayState;
	import java.JavaIntegration;
	import flash.system.Shell;
	import flash.display.NativeWindow;
	import flash.geom.Rectangle;
	import flash.display.Graphics;
	import mx.controls.Image;

	public class ButterflyNetBackend extends Sprite {

		// the component that displays ink on the stage...
		private var inkWell:Ink;
		
		private var debugText:TextArea;
		private var currInkStroke:InkStroke;

		private var theParent:ButterflyNet2;	
		private var theStage:Stage;
		private var theWindow:NativeWindow;
		
		private var slideTimer:Timer;
		private var javaBackend:JavaIntegration;

		// the port that Java is listening on
		private var portNum:int;
			
		private var imageControls:Array = new Array();


		public function ButterflyNetBackend(bnet:ButterflyNet2):void {
			trace("BNet Flash Backend Constructor");

			addListenerForCommandLineArguments();
			
			theParent = bnet;
			imageControls.push(theParent.img1);
			imageControls.push(theParent.img2);
			imageControls.push(theParent.img3);
			imageControls.push(theParent.img4);
			
			theStage = theParent.stage;
			theWindow = theParent.window;
			theWindow.width = 1024;
			theWindow.height = 768;
			theWindow.title = "ButterflyNet 2";			
			theWindow.addEventListener(Event.CLOSE, closing);
			
			inkWell = new Ink();
			addChild(inkWell);
			
			currInkStroke = new InkStroke();

			buttonMode = true;
			addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
			addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
		}
		
        private function onMouseDown(evt:Event):void {
            this.startDrag();
        }
        private function onMouseUp(evt:Event):void {
			this.stopDrag();
        }
		
		// either hide or show the large related items bar
		public function toggleRelatedItemsBar():void {
			if (theParent.currentState == "RelatedItemsHidden") {
				theParent.currentState = "";
			} else {
				theParent.currentState = "RelatedItemsHidden";
			}
		}

		private function closing(e:Event):void {
			trace("Closing...");
			javaBackend.send("exitApplication");
		}

		public function checkIfFirstTimeLoaded():void {
			theParent.currentState = "FirstTime";
			theParent.installDir.text = File.appResourceDirectory.nativePath;
		}

		public function addListenerForCommandLineArguments():void {
			Shell.shell.addEventListener(InvokeEvent.INVOKE, processCommandLineArguments);
		}
			
		// this is called after the command line arguments are processed
		private function start():void {
			javaBackend = new JavaIntegration(portNum);	
			javaBackend.addMessageListener(msgListener);
			javaBackend.send("connected");
		}

		// Exits the Application...		
		public function exit():void {
			javaBackend.send("exitServer");
			Shell.shell.exit();
		}

		// process the command line arguments
		public function processCommandLineArguments(invocation:InvokeEvent):void{
			var arguments:Array;
			var currentDir:File;
			
			arguments = invocation.arguments;
		    currentDir = invocation.currentDirectory;
		    
			trace("Current Directory: " + currentDir.nativePath);
		    if (arguments.length > 0) {
		    	// trace(arguments);
		    	for each (var arg:String in arguments) {
		    		trace(arg);
		    		
		    		if (arg.indexOf("port:") > -1) {
		    			portNum = parseInt(arg.substr(arg.indexOf("port:")+5));
		    			trace("Port: " + portNum);
		    		}
		    	}
		    }
		    start();
		}
			
		// TODO: Make the SlideShow Work!
		private function timerHandler(event:TimerEvent):void {
			next();
		}

		public function setVisibilityOfControls(vis:Boolean):void {
			theParent.showByComboBox.visible = vis;
			theParent.zoomInButton.visible = vis;
			theParent.zoomOutButton.visible = vis;
			theParent.zoomDefaultButton.visible = vis;
		}
		//////////////////////////////////////////////////////////////////////////////
		public function zoomIn():void {
			scaleX *= 1.25;
			scaleY *= 1.25;
		}

		public function zoomOut():void {
			scaleX *= .8;
			scaleY *= .8;
		}

		public function resetZoom():void {
			scaleX = 1;
			scaleY = 1;
			rescaleAndrecenter();
		}
		//////////////////////////////////////////////////////////////////////////////
		private function isFullScreen():Boolean {
			return theStage.displayState == StageDisplayState.FULL_SCREEN;
		}
		
		public function toggleFullScreen():void {
			trace("toggleFullScreen");
			if (isFullScreen()) {
				// stop slideshow
				// kill the timer
				slideTimer.stop();
				slideTimer = null;					
	
				stage.displayState = StageDisplayState.NORMAL;
				setVisibilityOfControls(true);
			} else {
				// start slideshow
				slideTimer = new Timer(1200);
				slideTimer.addEventListener(TimerEvent.TIMER, timerHandler);
				slideTimer.start();
	
				stage.displayState = StageDisplayState.FULL_SCREEN;
				setVisibilityOfControls(false);
			}
		}

		public function next():void {
			trace("Sending Next");
			javaBackend.send("next");
		}
		
		public function prev():void {
			trace("Sending Prev");
			javaBackend.send("prev");
		}
		// processes data sent over from the java code
        private function msgListener(event:DataEvent):void {
        	
        	var relatedFiles:Array = new Array();
        	theParent.relatedFiles.dataProvider = relatedFiles;
        	
        	var msgXML:XML = new XML(event.text);
			if (event.text.indexOf("<ink") == 0) {
	        	// trace(msgXML.toXMLString());
	        	var parser:InkRawXMLParser = new InkRawXMLParser(msgXML);
	        	if (inkWell != null) {
	        		removeChild(inkWell);
	        	}
	        	inkWell = parser.ink;
	        	addChildAt(inkWell, 0);
	        	rescaleAndrecenter();
	        	addPageDecorations();
			} else if (event.text.indexOf("<photosAndVideos")==0) {
				
				var rootPath:String = msgXML.@rootPath;
				trace("Photos And Videos: " + msgXML.@count + ", " + msgXML.@rootPath);
				// get all the photos (somewhere down the XML tree)
				var photos:XMLList = msgXML.descendants("photo");
				
				var maxToLoad:Number = imageControls.length;
				var indexToLoad:Number = (Math.random() * photos.length()) - maxToLoad;
				var i:Number = 0;
				var photosToLoad:Array = new Array();
				trace("Max to load: " + maxToLoad);
				for each (var photo:XML in photos) {
					i++;
					// trace(photo);
					var path:String = photo.@path;
					
					var photoFile:File = new File(rootPath + "\\" + path);
					relatedFiles.push(photoFile);

					if (photoFile.exists && i > indexToLoad && photosToLoad.length < maxToLoad) {
						trace("Should Load ["+path+"]");
						// load a random photo
						photosToLoad.push(photoFile);
					}
				}	
				loadPhotos(photosToLoad);
			} 
			else {
	        	trace(msgXML.toXMLString());
			}
        }

		public function loadPhotos(photosToLoad:Array):void {
			
			var index:Number = 0;
			for each (var photo:File in photosToLoad) {
				trace("Loading Photo: " + photo + " " + index + " " + imageControls[index]);
				var imgControl:Image = imageControls[index];
				imgControl.load(photo.url);
				index++;
			}
		}

		public function rescaleAndrecenter():void {
			// var rect:Rectangle = inkWell.getRect(theStage); // Bad
        	inkWell.x = -inkWell.minX + inkWell.paddingX;
        	inkWell.y = -inkWell.minY + inkWell.paddingY + inkWell.paddingY/3; // even more space
        	
        	// HACK: I can't figure this out
        	// There is either a bug with the minX and maxX,
        	// or some major misunderstanding by me of how this works (esp with scaling)
        	// OMG: I got it to work!
        	var w:Number = inkWell.maxX - inkWell.minX + (inkWell.paddingX * 2);
        	inkWell.x += (theStage.width - w)/2;
		}


		public function addPageDecorations():void {
			var decorations:Sprite = new Sprite();
			
			x = 0;
			y = 0;

			var g:Graphics = decorations.graphics;
			g.clear();
			g.beginFill(0x202020);
			g.lineStyle(0.5, 0x444444);
			
			var xMin:Number = inkWell.minX;
			var xMax:Number = inkWell.maxX;
			var yMin:Number = inkWell.minY;
			var yMax:Number = inkWell.maxY;
			var padding:Number = inkWell.paddingX;
			
			g.drawRect(xMin-padding, yMin-padding, (xMax-xMin)+(2*padding), (yMax-yMin)+(2*padding));
			g.endFill();
			
			inkWell.addChildAt(decorations, 0);
		}


		// manipulate how the strokes look on screen
		public function thinnerStrokes():void {
		}

		public function widerStrokes():void {
		}
	}

	/* 
	
	Notes for File API in Apollo
	
	In Flex Builder vs. Deployed:
	Resource (Builder): C:\Documents and Settings\Ron Yeh\My Documents\Projects\ButterflyNet2\bin
	Resource (Deployed): C:\Documents and Settings\Ron Yeh\Program Files\Stanford\ButterflyNet

	User: C:\Documents and Settings\Ron Yeh
	Storage: C:\Documents and Settings\Ron Yeh\Application Data\butterflynet.ButterflyNet\Local Store
	Documents: C:\Documents and Settings\Ron Yeh\My Documents
	Desktop: C:\Documents and Settings\Ron Yeh\Desktop
	Roots:
		A:\
		C:\
		D:\
		Z:\
				
	trace("User: " + File.userDirectory.nativePath);
	trace("Resource: " + File.appResourceDirectory.nativePath);
	trace("Storage: " + File.appStorageDirectory.nativePath);
	trace("Documents: " + File.documentsDirectory.nativePath);
	trace("Desktop: " + File.desktopDirectory.nativePath);
	trace("Roots:");
	for each (var f:File in File.listRootDirectories()) {
		trace(f.nativePath);
	}
	
	*/
}