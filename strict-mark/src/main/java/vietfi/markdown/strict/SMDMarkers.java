/*
 * Copyright 2024, Thach-Anh Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vietfi.markdown.strict;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * mark a series of markers of parsing process.
 * 
 * Each content can be parsed as:
 * 
 * [ Marker start, Content Start, Content End, Marker End]
 * 
 */
public class SMDMarkers {

	//markers value for bitwise operators
    protected static final int MARKER_START = 1 << 30; //turn on bit 30
    protected static final int CONTENT_START = 1 << 29; //turn on bit 29
    protected static final int CONTENT_STOP = 1 << 28; //turn on bit 28
    protected static final int MARKER_STOP = 1 << 27; //turn on bit 27

    
	/**
     * markers of output
     * each signed integer (32 bits) divided into 4 bytes, the meaning is
     * 
     * bits indexes (byte alignment):
     * 
     * 3 2          1    1     0
     * 10987654 32109876 54321098 76543210
     * 
     * -ABCDSSS SSSSPPPP PPPPPPPP PPPPPPPP
     * 
     * -: sign bit
     * A: Marker Start
     * B: Content Start
     * C: Content Stop
     * D: Marker Stop
     * 
     * SSSSSSS: state 7 bits (from 0 to 127)
     * 
     * To get state: s = v & 0x07F00000 >> 20;
     * 
     * PPP..: position of buffer. 20 bits addressable, support maximum buffer length is 1 million characters.
     */
    private final int[] markers;
    //fulfill index (or length of markers)
    private int fulfillIndex = 0;
    //cursor index
    private int cursor = 0;
    
    public SMDMarkers(int markerLength) {
        this.markers = new int[markerLength];
    }
    
    
    public SMDMarkers(int[] markers, int markerPos) {
        this.markers = markers;
        this.fulfillIndex = markerPos;
    }

    /**
     * The current markers
     * @return
     */
    public int[] markers() {
    	return Arrays.copyOf(markers, fulfillIndex);
    }
    
    public void resetMarkers() {
    	fulfillIndex = 0;
    	cursor = 0;
    	markers[0] = 0; //only 1 first are ok
    }
    
    public void setMarkerFulfill(int index) {
    	fulfillIndex = index;
    }
    
    /**
     * The length of added markers
     * @return
     */
    public int markedLength() {
    	return fulfillIndex;
    }
    
    /**
     * markers is empty
     * 
     * @return
     */
    public boolean isEmpty() {
    	return fulfillIndex == 0;
    }
    
    /**
     * Get state at index
     * 
     * @param index of markers
     * @return the state of marker
     */
	public int getMarkerState(int index) {
		return markerState(markers[index]);
	}
	
    /**
     * Get position of index
     * 
     * @param index
     * @return
     */
	public int getMarkerPosition(int index) {
		return markerPosition(markers[index]);
	}
	
    /**
     * is marker start
     * 
     * @param index
     * @return
     */
	public boolean isMarkerStart(int index) {
		return whetherStartStop(markers[index], MARKER_START);
	}
	
	/**
     * is content start
     * 
     * @param index
     * @return
     */
	public boolean isContentStart(int index) {
		return whetherStartStop(markers[index], CONTENT_START);
	}
	
	/**
     * is marker start
     * 
     * @param index
     * @return
     */
	public boolean isMarkerStop(int index) {
		return whetherStartStop(markers[index], MARKER_STOP);
	}
	
	/**
     * is marker start
     * 
     * @param index
     * @return
     */
	public boolean isContentStop(int index) {
		return whetherStartStop(markers[index], CONTENT_STOP);
	}
	
	public static int markerState(int v) {
		return (v & 0x07F00000) >> 20;
	}
	
	
	public static int markerPosition(int v) {
		return v & 0x0FFFFF;
	}
	
	public static boolean whetherStartStop(int v, int startStop) {
		return (v & startStop) == startStop;
	}
	
	public void addStartMarkerContent(int newState, int markerBegin, int contentBegin) {
    	if(contentBegin > markerBegin) {
	    	//pack marker with state and position
	    	markers[fulfillIndex++] = MARKER_START | newState << 20 | markerBegin;
	        markers[fulfillIndex++] = CONTENT_START | newState << 20 | contentBegin;
    	}
    	else { //same position for both
	        markers[fulfillIndex++] = MARKER_START | CONTENT_START | newState <<20 | contentBegin;	
    	}    		
    }
	
	public void addStopContentMarker(int currentState, int contentEnd, int markerEnd) {
		if(contentEnd < markerEnd) {
	    	markers[fulfillIndex++] = CONTENT_STOP | currentState <<20 | contentEnd;
	        markers[fulfillIndex++] = MARKER_STOP | currentState <<20 | markerEnd;
    	}
    	else {
    		//same position for both
	        markers[fulfillIndex++] = MARKER_STOP | CONTENT_STOP | currentState <<20 | markerEnd;
    	}
	}
	
	public void addStartMarker(int newState, int markerBegin) {
    	//only content
    	markers[fulfillIndex++] = MARKER_START | newState <<20 | markerBegin;
    }
	
	public void addStopMarker(int currentState, int markerEnd) {
    	//only content
		markers[fulfillIndex++] = MARKER_STOP | currentState <<20 | markerEnd;
    }
	
	public void addStartContent(int newState, int contentBegin) {
    	//only content
    	markers[fulfillIndex++] = CONTENT_START | newState <<20 | contentBegin;
    }
	
	public void addStopContent(int currentState, int contentEnd) {
    	//only content
		markers[fulfillIndex++] = CONTENT_STOP | currentState <<20 | contentEnd;
    }
	
	public void rollbackLastMarkerContentStart(int ofState) {
    	if(fulfillIndex == 0) //empty, do nothing
    		return;
    	int pos = fulfillIndex - 1;
    	int value = markers[pos];
    	int lastState = getMarkerState(pos);
    	//check for MARKER_START or CONTENT_START
    	if(lastState == ofState) {
    		if((value & MARKER_START) == MARKER_START) {//same for both
    			fulfillIndex = pos;
    			return;
	    	}
	    	else if(pos > 0 && (value & CONTENT_START) == CONTENT_START && (markers[pos-1] & MARKER_START) == MARKER_START) {
	    		fulfillIndex = pos - 1;
    			return;
	    	}
    	}
    }
    
	public void rollbackLastContentStart(int ofState) {
    	if(fulfillIndex == 0) //empty, do nothing
    		return;
    	int pos = fulfillIndex - 1;
    	int value = markers[pos];
    	int lastState = getMarkerState(pos);
    	//check for MARKER_START or CONTENT_START
    	if(lastState == ofState) {
    		if((value & MARKER_START) == MARKER_START) {//they are combined, remove only content start
    			markers[pos] = value ^ CONTENT_START;
    			return;
	    	}
	    	else if(pos > 0 && (value & CONTENT_START) == CONTENT_START && (markers[pos-1] & MARKER_START) == MARKER_START) {
	    		fulfillIndex = pos - 1;
    			return;
	    	}
    	}
    }
	
	public int cursor() {
		return this.cursor;
	}
	
	public int cursorState() {
		if(this.cursor >= 0 && this.cursor < this.fulfillIndex)
			return getMarkerState(cursor);
		throw new IllegalStateException();
	}
	
	public int cursorNextState() {
		if(this.cursor + 1 < this.fulfillIndex)
			return getMarkerState(cursor+1);
		return 0; //state none
	}
	
	public int cursorPosition1() {
		if(this.cursor >= 0 && this.cursor < this.fulfillIndex) {
			if(whetherStartStop(markers[cursor], CONTENT_START) || whetherStartStop(markers[cursor], MARKER_STOP))
				return markerPosition(markers[cursor]);
			//the first one
			if(cursor == 0 && //compacted in the pass 
				whetherStartStop(markers[cursor], CONTENT_STOP)) 
				return 0; // print from zero position
			return -1;
		}
			
		throw new IllegalStateException();
	}
	
	public int cursorPosition2() {
		if(this.cursor >= 0 && this.cursor < this.fulfillIndex) {
			if(this.cursor + 1 < this.fulfillIndex
					&& (whetherStartStop(markers[cursor+1], MARKER_START) || whetherStartStop(markers[cursor+1], CONTENT_STOP)))
				return markerPosition(markers[cursor+1]);
			//the last one
			if(this.cursor + 1 == this.fulfillIndex
					&& (whetherStartStop(markers[cursor], CONTENT_START) || whetherStartStop(markers[cursor], MARKER_STOP)))
				//maximum addressable
				return 0xFFFFF;
			//return current position
			return markerPosition(markers[cursor]);
		}
		throw new IllegalStateException();
	}
	
	/**
     * Checks the cursor is marker start?
     * 
     * @return
     */
	public boolean cursorIsMarkerStart() {
		if(this.cursor >= 0 && this.cursor < this.fulfillIndex)
			return whetherStartStop(markers[cursor], MARKER_START);
		throw new IllegalStateException();
	}
	
	/**
     * Checks cursor is content start?
     * 
     * @return
     */
	public boolean cursorIsContentStart() {
		if(this.cursor >= 0 && this.cursor < this.fulfillIndex)
			return whetherStartStop(markers[cursor], CONTENT_START);
		throw new IllegalStateException();
	}
	
	/**
     * Checks the cursor is marker stop
     * 
     * @return
     */
	public boolean cursorIsMarkerStop() {
		if(this.cursor >= 0 && this.cursor < this.fulfillIndex)
			return whetherStartStop(markers[cursor], MARKER_STOP);
		throw new IllegalStateException();
	}
	
	/**
     * Checks the cursor is content stop
     * 
     * @return
     */
	public boolean cursorIsContentStop() {
		if(this.cursor >= 0 && this.cursor < this.fulfillIndex)
			return whetherStartStop(markers[cursor], CONTENT_STOP);
		throw new IllegalStateException();
	}
	
	/**
	 * move the cursor forward one, to check the next of close marker or open content for print out
	 * 
	 * I don't use Iterator interface. But i will consider to upgrade if applicable for the "double" moving 
	 * 
	 * @return true if moved
	 */
	public boolean cursorGoNext() {
		if(this.cursor >= this.fulfillIndex)
			return false;
		
		this.cursor++;
		return true;
	}
	
	public boolean cursorIsAvailable() {
		return this.cursor >= 0 && this.cursor < this.fulfillIndex;
	}
	
	public boolean cursorCanGoNext() {
		return this.cursor < this.fulfillIndex;
	}
	
	public void cursorReset(int cursor) {
		this.cursor = cursor;
	}
	
	public void cursorReset() {
		cursorReset(0);
	}
	
	public boolean cursorIsCompactable(int shiftRemaining) {
		if(cursor == fulfillIndex || fulfillIndex == 0)
    		return true;
		return shiftRemaining <= markerPosition(markers[cursor]);
	}
	
	/**
	 * 
	 * Reduce position of all markers by shiftRemaining. This method using before the call of CharBuffer.compact()
	 * for adjusting the position of markers to correct index of the buffer. 
	 * 
	 * @param shiftRemaining = charBuffer.position() before calling charBuffer.position().
	 * @return number of reduced marker items.
	 */
    public int compactMarkers(int shiftRemaining) {
    	if(fulfillIndex == 0)
    		return -1;
    	if(shiftRemaining < 0)
    		throw new IllegalArgumentException();
    	int last = 0;
    	if(shiftRemaining > markerPosition(markers[fulfillIndex - 1])) { //shift larger than the last marker's position, remove all
    		last = fulfillIndex;
    		this.fulfillIndex = 0;
    		this.cursor = 0; //reset to first
    		return last;
    	}
    	
    	for(int i = 0; i < this.fulfillIndex; i++) {
    		int pos = markerPosition(markers[i]) - shiftRemaining;
    		if(pos < 0)
    			last++;
    		else {
	    		//32 bits unsigned = 0x7FFFFFFF
	    		markers[i] = (markers[i] & 0x7FF00000) | pos;
    		}
    	}
    	
    	if(last > 0) { //shift arrays of negative position
    		for(int i = last; i < this.fulfillIndex; i++)
    			markers[i-last] = markers[i];
    		this.fulfillIndex = fulfillIndex - last;
    		if(this.cursor > last)
    			this.cursor = cursor - last;
    		else
    			this.cursor = 0;
    	}
    	return last;
    }
 
    @Override
    public String toString() {
    	if(fulfillIndex == 0)
    		return "Markers[0]: empty\n";
    	
        StringBuilder sb = new StringBuilder();
        
    	int start = getMarkerPosition(0);
    	int stop = getMarkerPosition(fulfillIndex - 1);
        
    	sb.append("Markers[").append(fulfillIndex).append("]: start at ").append(start)
    		.append(", end at ").append(stop).append(", string length=").append(stop-start+1).append("\n");
    	
    	IntFunction<String> op = new IntFunction<String>() {
        	StringBuilder sb = new StringBuilder();
			@Override
			public String apply(int value) {
				sb.setLength(0);
				if((value & MARKER_START) == MARKER_START)
					sb.append("<");
				if((value & CONTENT_START) == CONTENT_START)
					sb.append("[");
				if((value & CONTENT_STOP) == CONTENT_STOP)
					sb.append("]");
				if((value & MARKER_STOP) == MARKER_STOP)
					sb.append(">");
				
				sb.append(SMDMarkers.markerState(value));
				return sb.toString();
			}
        	
        };
        int[] only = this.markers();
        sb.append(Arrays.stream(only).mapToObj(op).collect(Collectors.joining("\t"))).append("\n");
        sb.append(Arrays.stream(only).mapToObj(v -> String.valueOf(v & 0x0FFFFF)).collect(Collectors.joining("\t"))).append("\n");
        
        return sb.toString();
    }


	public int remaining() {
		return markers.length - fulfillIndex;
	}


}
