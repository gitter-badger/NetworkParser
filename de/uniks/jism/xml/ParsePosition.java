package de.uniks.jism.xml;

import de.uniks.jism.Style;

/*
 Copyright (c) 2013, Stefan Lindel
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 3. All advertising materials mentioning features or use of this software
 must display the following acknowledgement:
 This product includes software developed by Stefan Lindel.
 4. Neither the name of contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE 'Json Id Serialisierung Map' IS PROVIDED BY STEFAN LINDEL ''AS IS'' AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL STEFAN LINDEL BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


/**
 * The parser parsed result.
 * 
 * This class include the information needed to highlight the syntax.
 * Information includes where the content located in the document (offset and
 * length) and what style should be applied on that segment of content.
 */
public class ParsePosition {
	/**
	 * The start position of the content.
	 */
	protected int offset;
	/**
	 * The length of the content.
	 */
	protected int length;

	/**
	 * The Typ of the Style
	 */
	private String styleTyp;
	private Style style;
	/**
	 * Constructor.
	 * 
	 * @param offset
	 *            the start position of the content
	 * @param length
	 *            the length of the content
	 */
	public ParsePosition(int offset, int length, Style style) {
		this.offset = offset;
		this.length = length;
		this.style= style;
	}

	/**
	 * Constructor.
	 * 
	 * @param offset
	 *            the start position of the content
	 * @param length
	 *            the length of the content
	 * @param typ
	 *            the typ of the PrsePosition
	 */
	public ParsePosition(int offset, int length, String typ, Style style) {
		this.offset = offset;
		this.length = length;
		this.styleTyp = typ;
		this.style= style;
	}
	/**
	 * The start position of the content.
	 * 
	 * @return the start position of the content
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * The length of the content.
	 * 
	 * @return the length of the content
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @return the Typ of Style
	 */
	public String getStyleTyp() {
		return styleTyp;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[");
		sb.append(offset);
		sb.append("-");
		sb.append(offset+length);
		sb.append(": ");
		sb.append(styleTyp);
		sb.append("]");
		return sb.toString();
	}

	public Style getStyle() {
		return style;
	}
}