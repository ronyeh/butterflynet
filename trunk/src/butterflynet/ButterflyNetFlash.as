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

	public class ButterflyNetFlash extends Sprite {

		private var inkWell:Ink;
		private var sock:XMLSocket;
		private var debugText:TextArea;
		private var currInkStroke:InkStroke;
		
		private var theParent:ButterflyNet2;	
			
		public function addParent(bnet:ButterflyNet2):void {
			theParent = bnet;
		}
			
		public function thinnerStrokes():void {
			
		}

		public function widerStrokes():void {

		}
			
		public function next():void {
			
			theParent.img1.visible = (Math.random() > 0.5);
			theParent.img2.visible = (Math.random() > 0.5);
			theParent.img3.visible = (Math.random() > 0.5);
			theParent.img4.visible = (Math.random() > 0.5);
			theParent.img5.visible = (Math.random() > 0.5);
			theParent.img6.visible = (Math.random() > 0.5);
			
			trace("Sending Next");
			sock.send("<Next/>\n");
		}
		public function prev():void {
			trace("Sending Prev");
			sock.send("<Prev/>\n");
		}
			
		public function ButterflyNetFlash():void {
			trace("constructor!");
			inkWell = new Ink();
			addChild(inkWell);
			
			currInkStroke = new InkStroke();
			
			trace("Ink Client Started.");
			startListening();
		}


 		public function startListening():void {
			sock = new XMLSocket();
			configureListeners(sock);
			
			flash.system.Security.loadPolicyFile("xmlsocket://localhost:7545");
			sock.connect("localhost", 7545);
		}

        private function configureListeners(dispatcher:IEventDispatcher):void {
            dispatcher.addEventListener(Event.CLOSE, closeHandler);
            dispatcher.addEventListener(Event.CONNECT, connectHandler);
            dispatcher.addEventListener(DataEvent.DATA, dataHandler);
            dispatcher.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
            dispatcher.addEventListener(ProgressEvent.PROGRESS, progressHandler);
            dispatcher.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
        }

        private function closeHandler(event:Event):void {
            trace("closeHandler: " + event);
        }

        private function connectHandler(event:Event):void {
            trace("connectHandler: " + event);
			sock.send("connected\n");
        }

		public function recenter():void {
			inkWell.recenter();
		}

        private function dataHandler(event:DataEvent):void {
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
        
        private function ioErrorHandler(event:IOErrorEvent):void {
            trace("ioErrorHandler: " + event);
        }

        private function progressHandler(event:ProgressEvent):void {
            trace("progressHandler loaded:" + event.bytesLoaded + " total: " + event.bytesTotal);
        }

        private function securityErrorHandler(event:SecurityErrorEvent):void {
            trace("securityErrorHandler: " + event);
        }
 
	}
	
}