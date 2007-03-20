package butterflynet
{

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
			
			
		public function thinnerStrokes():void {
						
		}

		public function widerStrokes():void {

		}
			
		public function next():void {
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
        }

		public function recenter():void {
			inkWell.recenter();
		}

        private function dataHandler(event:DataEvent):void {
        	var pagesXML:XML = new XML(event.text);
        	// trace(pagesXML.toXMLString());
        	
        	var parser:InkRawXMLParser = new InkRawXMLParser(pagesXML);
        	if (inkWell != null) {
        		removeChild(inkWell);
        	}
        	inkWell = parser.ink;
        	addChild(inkWell);
        }
        
        private function dataHandlerOLD(event:DataEvent):void {
            //trace("dataHandler: " + event);
            // trace(event.text); // parse the text and assemble InkStrokes...
            
            var inkXML:XML = new XML(event.text);
            // trace("XML: " + inkXML.toXMLString());

			trace(inkXML.@x + " " + inkXML.@y + " " + inkXML.@f + " " + inkXML.@t + " " + inkXML.@p);

			var xVal:Number = parseFloat(inkXML.@x);
			var yVal:Number = parseFloat(inkXML.@y);

			var penUp:Boolean = inkXML.@p == "U";
			if (penUp) {
				// add to the inkWell
				inkWell.addStroke(currInkStroke);

				// reposition it to the minimum (with some padding) after each stroke
				inkWell.recenter();

				// start up a new stroke
   				currInkStroke = new InkStroke();
			} else {
				// add samples to the current stroke
				currInkStroke.addPoint(xVal, yVal, parseFloat(inkXML.@f));
			}		
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