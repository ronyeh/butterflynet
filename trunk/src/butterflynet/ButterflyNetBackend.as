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

	public class ButterflyNetBackend extends Sprite {

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
			
		public function ButterflyNetBackend(bnet:ButterflyNet2):void {
			trace("BNet Flash Backend Constructor");

			addListenerForCommandLineArguments();
			
			theParent = bnet;
			theStage = theParent.stage;
			theWindow = theParent.window;
			theWindow.width = 1024;
			theWindow.height = 768;
			theWindow.title = "ButterflyNet 2";			
			theWindow.addEventListener(Event.CLOSE, closing);
			
			inkWell = new Ink();
			addChild(inkWell);
			
			currInkStroke = new InkStroke();
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
			recenter();
		}
		public function recenter():void {
			trace(inkWell.getBounds(this));
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
        private function msgListener(event:DataEvent):void {
        	var pagesXML:XML = new XML(event.text);
        	// too much data to trace all the time =)
        	// trace(pagesXML.toXMLString());
        	
        	var parser:InkRawXMLParser = new InkRawXMLParser(pagesXML);
        	if (inkWell != null) {
        		removeChild(inkWell);
        	}
        	inkWell = parser.ink;
        	addChild(inkWell);
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