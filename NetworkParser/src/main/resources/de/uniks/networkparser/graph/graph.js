/*
 NetworkParser
 Copyright (c) 2011 - 2014, Stefan Lindel
 All rights reserved.
 
 Licensed under the EUPL, Version 1.1 or (as soon they
 will be approved by the European Commission) subsequent
 versions of the EUPL (the "Licence");
 You may not use this work except in compliance with the Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl5

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.
*/
Object_create = Object.create || function (o) { var F = function() {};F.prototype = o; return new F();}
/* Pos */
Pos = function(x, y, id) {this.x = x || 0; this.y = y || 0; if(id){this.id = id;} }
/* GraphUtil */
GraphUtil = function(ns){if(ns) {this.ns = ns;}}
GraphUtil.prototype.copy = function(ref, src, full) {
	if(src){
		for(var i in src){
			if(i.charAt(0)=="_" || i == "htmlElement"){
				if(full){ref[i] = src[i];}
				continue;
			}
			if(typeof(src[i])=="object") {
				if(!ref[i]){
					if(src[i] instanceof Array) {
						ref[i] = [];
					}else{
						ref[i] = {};
					}
				}
				this.copy(ref[i], src[i], full);
			}else{
				ref[i] = src[i];
			}
		}
	}
	return ref;
}
GraphUtil.prototype.bind = function(el, eventName, eventHandler) {
	if (el.addEventListener){
		el.addEventListener(eventName, eventHandler, false); 
	} else if (el.attachEvent){
		el.attachEvent('on'+eventName, eventHandler);
	}
}
GraphUtil.prototype.create = function(node){
	var item;
	
	if(document.createElementNS && (node.xmlns || this.ns )){
		if(node.xmlns) {
			item = document.createElementNS(node.xmlns, node.tag);
		} else {
			item = document.createElementNS(this.ns, node.tag);
		}
	} else {
		item = document.createElement(node.tag);
	}
	var tag = node.tag.toLowerCase();
	for (var key in node) {
		var k = key.toLowerCase();
		if(node[key] == null) continue;
		if(k=='tag' || k=='content_src' || k.charAt(0)=='_'	|| k=='model') continue;
		if(k=='rotate'){
			item.setAttribute("transform", "rotate("+node[key]+","+node.x+","+node.y+")");
			continue;
		}
		if(k=='value'){
			if(tag!="input"){
				if(tag=="text") {// SVG
					item.appendChild(document.createTextNode(node[key]));
				}else{
					item.innerHTML = node[key];
				}
			}else {
				item[key] = node[key];
			}
			continue;
		}
		if(k.indexOf("on") == 0) {
			this.bind(item, k.substring(2), node[key]);
			continue;
		}
		if(k.indexOf("-")>=0){
			item.style[key] = node[key];
		}else {
			if(k=="style" && typeof(node[key])=="object"){
				for (var style in node[key]) {
					if(node[key][style]){
						if("transform"===style){
							item.style.transform = node[key][style];
							item.style.msTransform = item.style.MozTransform = item.style.WebkitTransform = item.style.OTransform= node[key][style];
						} else {
							item.style[style] = node[key][style];
						}
					}
				}
			}else{
				item.setAttribute(key, node[key]);
			}
		}
	}
	if(node._font) {
		if(this.model && this.model.options && this.model.options.font) {
			for (var key in this.model.options.font) {
				if(this.model.options.font[key]){
					if(item.style){
						item.style[key] = this.model.options.font[key];
					}else{
						item.setAttribute(key, this.model.options.font[key]);
					}
				}
			}
		}
	}

	if(node._parent) {
		node._parent.appendChild(item);
	}
	if(tag=="image" && node["content_src"]) {
		item.setAttribute('xmlns:xlink', "http://www.w3.org/1999/xlink");
		item.setAttributeNS("http://www.w3.org/1999/xlink", 'href', node["content_src"]);
	}
	if(node.model) {
		item.model = node.model;
	}
	return item;
}
GraphUtil.prototype.getModel = function(element) {
	while(!element.model) {
		if(element.parentElement){
			return this.getModel(element.parentElement);
		}
		return null;
	}
	return element;
}
GraphUtil.prototype.getValue = function(value) {return parseInt(("0"+value).replace("px",""));}
GraphUtil.prototype.isIE = function() {return document.all&&!window.opera;}

//FIXME
GraphItem = function(id) { this.init();this.typ = "node";this.id = id;}
GraphItem.prototype = Object_create(GraphUtil.prototype);
GraphItem.prototype.getX = function(){
	if(this._graph){return this.x + this._graph.getX();}
	return this.x;
}
GraphItem.prototype.getY = function(){
	if(this._graph){return this.y + this._graph.getY();}
	return this.y;
}
GraphItem.prototype.getRoot = function() {
	if(this._graph){return this._graph.getRoot();}
	return this;
}

GraphModel=function(json, options, parent){
	this.typ = "classdiagram";
	this._parent = parent;
	json = json || {};
	this.left=json.left || 0;
	this.top=json.top || 0;
	this.x = this.y = this.width = this.height=0;
	this.nodeCount=0;
	this.nodes = {};
	this.edges = [];
	json = json || {};
	this.typ = json.typ || "classdiagram";
	this.options = this.merge(this.merge(new Options(), json.options), options, true);
	this.package = "";
	if(json.info){
		this.info = json.info;
	}
	if(json.style){
		this.style = json.style;
	}
	if(json.nodes) {
		for (var i = 0; i < json.nodes.length; i++) {
			this.addNode(json.nodes[i]);
		}
	}
	if(json.edges) {
		for (var i = 0; i < json.edges.length; i++){
			var e = json.edges[i];
			var edge;
			var typ=e.typ.charAt(0).toUpperCase()+e.typ.substring(1).toLowerCase();
			if(typeof window[typ] === "function") {
				edge = eval("new "+typ+"()");
			}else {
				edge = new Edge();
			}
			edge.sourceInfo = new Info(e.source, this, edge);
			edge.targetInfo = new Info(e.target, this, edge);
			edge.source = this.getNode(edge.sourceInfo.id);
			edge.info = e.info;
			edge.style = e.style;
			edge.source.edges.push(edge);
			edge.model = this;
			edge.target = this.getNode(edge.targetInfo.id);
			edge.target.edges.push(edge);
			this.edges.push(edge);
		}
	}
}
GraphModel.prototype = Object_create(GraphItem.prototype);
GraphModel.prototype.merge = function(ref, src, full) {
	if(src){
		for(var i in src){
			if(i.charAt(0)=="_"){
				if(full){ref[i] = src[i];}
				continue;
			}
			if(typeof(src[i])=="object" && typeof(ref[i])=="object"){
				this.merge(ref[i], src[i])
			}else{
				ref[i] = src[i];
			}
		}
	}
	return ref;
}
GraphModel.prototype.addNode = function(node) {
	/* testing if node is already existing in the graph */
	if(typeof(node)=="string"){
		node = {id:node, typ:"node"};
	}
	node.typ = node.typ.toLowerCase();
	if(!(node.id)){
		node.id = node.typ+"_"+(this.nodeCount+1);
	}
	if(this.nodes[node.id] != undefined) {
		return this.nodes[node.id];
	}
	if(node.typ.indexOf("diagram", node.typ.length - 7) !== -1) {
		node = new GraphModel(this._graph, node, new Options(this));
	}else{
		node = this.copy(new GraphNode(), node);
	}
	this.nodes[node.id] = node;
	node._parent = this;
	this.nodeCount++;
	return this.nodes[node.id];
}
GraphModel.prototype.addEdge = function(source, target) {
	var edge = new Edge();
	edge.source = this.addNode(source);
	edge.target = this.addNode(target);
	edge.source.edges.push(edge);
	this.edges.push(edge);
	edge.target.edges.push(edge);
	return edge;
}
GraphModel.prototype.removeNode = function(id) {
	if(this.nodes[id].htmlNode){
		this._board.removeChild(this.nodes[id].htmlNode);
	}
	delete(this.nodes[id]);

	for(var i = 0; i < this.edges.length; i++) {
		if (this.edges[i].source.id == id || this.edges[i].target.id == id) {
			this.edges.splice(i, 1);
			i--;
		}
	}
}
GraphModel.prototype.getNode = function(id, isSub) {
	if(this.nodes[id]) {
		return this.nodes[id];
	}
	for(var i = 0;i < this.options._subgraphs.length;i++){
		var r = this.options._subgraphs[i].getNode(id, true);
		if(r) {
			return r;
		}
	}
	if(!isSub){
		return this.addNode(id);
	}
	return null;
}
GraphModel.prototype.toJson = function() {return this.copy({}, this);}
GraphModel.prototype.createElement = function(element, typ){this._parent.createElement(element, typ);}
GraphModel.prototype.removeFromBoard = function(board){if(this.htmlNode){board.removeChild(this.htmlNode);this.htmlNode = null;}}
/* Info */
Info = function(info, parent, edge) { 
	if(typeof(info)==="string"){
		this.id = info; 
	}else{
		this.property = info.property; 
		this.cardinality = info.cardinality; 
		this.id = info.id; 
	}
	this.typ = "Info";
	this.x = this.y = this.width = this.height = 0;
	this.center=new Pos();
	this.custom = false;
	this._parent = parent;
	this.edge = edge;
	this.isdraggable = true;
}

Line = function(source, target, line, style) {this.source = source; this.target = target; this.line = line; this.style = style;}
Line.Format={SOLID:"SOLID", DOTTED:"DOTTED"}
/* Options */
Options = function(){
	this.canvasid = null;
	this.raster = false;
	this.display = "svg";
	this.font={"font-size":"10px", "font-family": "Verdana"};
	this.layout= {name:"Dagre", rank:"TB", nodesep:10}	// Dagre TB, LR
	this.CardinalityInfo = true;
	this.PropertyInfo = true;
	this.rotateText = true;
	this.buttons = ["HTML", "SVG"];	// ["HTML", "SVG", "PNG", "PDF"]
}
/* Node */
GraphNode = function(id) { this.init();this.typ = "node";this.id = id;}
GraphNode.prototype = Object_create(GraphItem.prototype);
GraphNode.prototype.init = function() {
	this.edges = [];
	this.attributes = [];
	this.methods = [];
	this.RIGHT = this.LEFT = this.UP = this.DOWN=0;
	this.x = this.y = this.width = this.height=0;
	this.isdraggable = true;
}
GraphNode.prototype.removeFromBoard = function(board){if(this.htmlNode){board.removeChild(this.htmlNode);this.htmlNode = null;}}














//				######################################################### Graph #########################################################
Graph = function(json, options) {
	this.init();
	this.model = new GraphModel(json, options);
	this.initLayouts();
	this.loader = new Loader(this);
	this.htmlNodes = {};
	if((""+this.model.options.display).toLowerCase()=="html"){
		this.drawer = new HTMLDrawer();
		this.loader.init(true);
	}else{
		this.drawer = new SVGDrawer();
		this.model.options.display = "svg";
	}
	var layout = this.layouts[0];

	for(var i=0;i<this.layouts.length;i++){
		if(this.layouts[i]["name"] === this.model.options.layout.name.toLowerCase()){
			layout = this.layouts[i];
			break;
		}
	}
	this.layouter = layout.value;
	this.subgraphs = [];
	if(this.model.options.canvasid){
		this.root = document.getElementById(this.model.options.canvasid);
	}
	if(!this.root){
		this.root = document.createElement("div");
		if(this.model.options.canvasid){
			this.root.id = this.model.options.canvasid;
		}
		document.body.appendChild(this.root);document.body.appendChild(this.root);
	}
}
Graph.prototype = Object_create(GraphNode.prototype);
Graph.prototype.initLayouts=function(){ this.layouts=[{name:"dagre", value:new DagreLayout()}];}
Graph.prototype.removeFromBoard = function(board){
	if(this.htmlNode){
		board.removeChild(this.htmlNode);
		this.htmlNode = null;
	}
	this.board = null;
}
Graph.prototype.copy = function(source, target){
	this.merge(target, source);
	if(source.width){target.startWidth = source.width;}
	if(source.height){target.startHeight = source.height;}
	return target;
}
Graph.prototype.initInfo = function(edge, info){
	if(!this.model.options.CardinalityInfo && !this.model.options.PropertyInfo){
		return null;
	}	
	var infoTxt = edge.getInfo(info);
	if(infoTxt.length > 0) {
		var html = this.drawer.createInfo(info, true, infoTxt, 0);
		if(html){
			var pos = this.getDimension(html);
			info.width = pos.x;
			info.height = pos.y;
		}
	}
	return infoTxt;
}
Graph.prototype.clearBoard = function(){
	if(this.board){
		this.clearLines();
		for(var i in this.model.nodes) {
			var n=this.model.nodes[i];
			n.removeFromBoard(this.board);
			n.RIGHT = n.LEFT = n.UP = n.DOWN=0;
		}
		this.root.removeChild(this.board);
	}
	this.drawer.clearBoard();
}
Graph.prototype.getDimension = function(html){
	if(this._parent){
		return this._parent.getDimension(html);
	}
	if(!this.board){
		return new Pos();
	}
	this.board.appendChild(html);
	var rect = html.getBoundingClientRect();
	var pos = new Pos(rect.width, rect.height);
	this.board.removeChild(html);
	return pos;
}
Graph.prototype.addNode = function(node) {return this.model.addNode(node);}
Graph.prototype.addEdge = function(source, target) {return this.model.addEdge(source, target);}
Graph.prototype.removeNode = function(id) {return this.model.removeNode(id);}
Graph.prototype.calcLines = function(){
	var ownAssoc = [];
	for(var i in this.model.nodes) {
		var n = this.model.nodes[i];
		n.RIGHT = n.LEFT = n.UP = n.DOWN=0;
	}
	for(var i = 0; i < this.model.edges.length; i++) {
		if(!this.model.edges[i].calculate(this.board, this.drawer)){
			ownAssoc.push(this.model.edges[i]);
		}
	}
	for(var i = 0; i < ownAssoc.length; i++) {
		ownAssoc[i].calcOwnEdge();
		var sourcePos = ownAssoc[i].getCenterPosition(ownAssoc[i].source, ownAssoc[i].start);
		ownAssoc[i].calcInfoPos( sourcePos, ownAssoc[i].source, ownAssoc[i].sourceInfo);

		sourcePos = ownAssoc[i].getCenterPosition(ownAssoc[i].target, ownAssoc[i].end);
		ownAssoc[i].calcInfoPos( sourcePos, ownAssoc[i].target, ownAssoc[i].targetInfo);
	}
}
Graph.prototype.drawLines = function(model){
	this.clearLines(model);
	for(var i = 0; i < model.edges.length; i++) {
		model.edges[i].draw(model._board, this.drawer);
	}
}
Graph.prototype.clearLines = function(model){
	for(var i=0; i<model.edges.length;i++) {
		var e = model.edges[i];
		if(e.htmlElement){
			while(e.htmlElement.length>0){
				model._board.removeChild(e.htmlElement.pop());
			}
		}
	}
}
Graph.prototype.MinMax = function(node, min, max){
	max.x = Math.max(max.x,node.x+node.width+10);
	max.y=Math.max(max.y,node.y+node.height+10);
	min.x=Math.max(min.x,node.x);
	min.y=Math.max(min.y,node.y);
}
Graph.prototype.resize = function(model){
	var min=new Pos();
	var max=new Pos(model.minSize.x, model.minSize.y);
	for (var i in model.nodes) {
		var n = model.nodes[i];
		this.moveToRaster(n);
		this.MinMax(n, min, max);
	}
	this.calcLines();
	for(var i=0;i<model.edges.length;i++){
		var e=model.edges[i];
		this.MinMax(e.sourceInfo, min, max);
		this.MinMax(e.targetInfo, min, max);
	}
	this.drawer.setSize(model._board, max.x, max.y);
	if(model.options.raster){
		this.drawRaster();
	}
	this.drawLines(model);
	return max;
}
Graph.prototype.drawRaster = function(){
	while(this.board.rasterElements.length>0){
		this.board.removeChild(this.board.rasterElements.pop());
	}
	var width = this.board.style.width.replace("px","");
	var height = this.board.style.height.replace("px","");
	for(var i=10;i<width;i+=10){
		var line = this.drawer.createLine(i, 0, i, height, null, "#ccc");
		line.className="lineRaster";
		this.board.rasterElements.push(line);
		this.board.appendChild(line);
	}
	for(var i=10;i<height;i+=10){
		var line = this.drawer.createLine(0, i, width, i, null, "#ccc");
		line.className="lineRaster";
		this.board.rasterElements.push(line);
		this.board.appendChild(line);
	}
}
Graph.prototype.draw = function(model, width, height){
	for(var i in model.nodes) {
		var n = model.nodes[i];
		if(model.left>0 || model.top > 0) {
			n.x += model.left;
			n.y += model.top;
		}
	}
	this.drawGraph(model, width, height);
}
Graph.prototype.drawGraph = function(model, width, height){
	model.minSize = new Pos(width || 0, height || 0);
	if(this.loader.abort && this.loader.images.length>0){
		return;
	}
	if(!model._board) {
		this.initGraph(model);
	}
	this.resize(model);

	for(var i in model.nodes) {
		var n = model.nodes[i];
		n.htmlNode = this.drawer.getNode(n);
		if(n.htmlNode){
			model._board.appendChild( n.htmlNode );
		}
	}
}
Graph.prototype.moveToRaster = function(node){
	if(this.model.options.raster){
		node.x = parseInt(node.x / 10) * 10;
		node.y = parseInt(node.y / 10) * 10;
		if(node.htmlNode){
			this.drawer.setPos(node.htmlNode, node.x, node.y);
		}
	}
}
Graph.prototype.layout = function(minwidth, minHeight, model){
	model = model || this.model;
	for (var i in model.nodes) {
		var n = model.nodes[i];
		var isDiag = n.typ.indexOf("diagram", n.typ.length - 7) !== -1;
		if(isDiag) {
			this.initGraph(n);
		}
		var html = this.drawer.getNode(n);
		if(html){
			var pos = this.getDimension(html);
			if(!n.startWidth){
				n.width=pos.x;
			}
			if(!n.startHeight){
				n.height=pos.y;
			}
			this.htmlNodes[n.id] =html;
			// FIXME
					this.options._subgraphs.push(node);

		}
		if(isDiag){
			n.center = new Pos(n.x + (n.width / 2), n.y + (n.height / 2));
		}
	}
	for (var i=0; i< model.edges.length;i++) {
		var e = model.edges[i];
		this.initInfo(e, e.sourceInfo);
		this.initInfo(e, e.targetInfo);
	}
	this.layouter.layout(this, model, Math.max(minwidth || 0, 100), Math.max(minHeight || 0, 100));
}
Graph.prototype.createElement = function(element, typ){
}
//				######################################################### DRAG AND DROP #########################################################
Graph.prototype.initDragAndDrop = function(){
	this.objDrag = null;
	this.mouse = new Pos();
	this.offset= new Pos();
	this.startObj= new Pos();
	var that = this;
	this.bind(this.model._board, "mousemove", function(e){that.doDrag(e);});
	this.bind(this.model._board, "mouseup", function(e){that.stopDrag(e);});
	this.bind(this.model._board, "mouseout", function(e){that.stopDrag(e);});
}
Graph.prototype.addNodeLister = function(element, node){
	var that = this;
	element.node = node;
	this.bind(element, "mousedown", function(e){that.startDrag(e);});
}
Graph.prototype.setSelectable = function(node, value) {
	if (node.nodeType == 1) {
		if(value){
			node.setAttribute("unselectable", value);
		}else{
			node.removeAttribute("unselectable");
		}
	}
	var child = node.firstChild;
	while (child) {
		this.setSelectable(child, value);
		child = child.nextSibling;
	}
}
Graph.prototype.getDragNode = function(node) {
	if(node.model){
		if(!node.model.isdraggable){
			return null;
		}
		return node;
	}else if(node.parentElement.model) {
		if(!node.parentElement.model.isdraggable){
			return null;
		}
		return node.parentElement;
	}
	return null;
}
Graph.prototype.startDrag = function(event) {
	var n = this.getDragNode(event.currentTarget);
	if(!n){
		return;
	}
	if(this.objDrag){
		return;
	}
	this.objDrag = n;
	var graph = this.objDrag.parentElement;
	if(graph) {
		for(var i=0;i<graph.children.length;i++) {
			this.setSelectable(graph.children[i], "on");
		}
	}
	this.offset.x = this.isIE ? window.event.clientX : event.pageX;
	this.offset.y = this.isIE ? window.event.clientY : event.pageY;
	this.startObj.x = this.objDrag.model.x;
	this.startObj.y = this.objDrag.model.y;
}
Graph.prototype.doDrag = function(event) {
	this.mouse.x = this.isIE ? window.event.clientX : event.pageX;
	this.mouse.y = this.isIE ? window.event.clientY : event.pageY;

	if (this.objDrag != null) {
		var x =(this.mouse.x - this.offset.x) + this.startObj.x;
		var y =(this.mouse.y - this.offset.y) + this.startObj.y;

		if(this.model.options.display=="svg"){
			x = x - this.startObj.x;
			y = y - this.startObj.y;
			this.objDrag.setAttribute('transform', "translate("+x+" "+y+")");
		} else {
			this.drawer.setPos(this.objDrag, x, y);
			if(this.objDrag.model){
				this.objDrag.model.x = x;
				this.objDrag.model.y = y;
				this.objDrag.model._parent.resize(this.model);
			}
		}
	}
}
Graph.prototype.stopDrag = function(event) {
	if(!this.objDrag){
		return;
	}
	if(!(event.type=="mouseup"||event.type=="mouseout")&&!event.currentTarget.isdraggable){
		return;
	}
	var item = this.objDrag;
	this.objDrag = null;
	var graph = item.parentElement;
	if(graph) {
		for(var i=0;i<graph.children.length;i++) {
			this.setSelectable(graph.children[i], null);
		}
	}
	if(item.model){
		if(this.model.options.display=="svg"){
			if(item.getAttributeNS(null, "transform")){
				var pos = item.getAttributeNS(null, "transform").slice(10,-1).split(' ');
				item.model.x = item.model.x + Number(pos[0]);
				item.model.y = item.model.y + Number(pos[1]);
			}
			item.model.center = new Pos(item.model.x + (item.model.width / 2), item.model.y + (item.model.height / 2));
			
			this.board.removeChild(item);
			if(item.model.board) {
				item.model.board = null;
			}

			if(item.model.typ=="Info") {
				item.model.custom = true;
				item.model.edge.removeElement(item);
				var infoTxt = item.model.edge.getInfo(item.model);
				item.model.edge.addElement(this.board, this.drawer.createInfo(item.model, false, infoTxt));
			}else{
				item.model.htmlNode = this.drawer.getNode(item.model);
				if(item.model.htmlNode){
					this.board.appendChild( item.model.htmlNode );
				}
				for(var i=0;i<item.model.edges.length;i++){
					var edge = item.model.edges[i];
					edge.sourceInfo.custom = false;
					edge.targetInfo.custom = false;
				}
			}
		}
		this.resize(this.model);
	}
}
Graph.prototype.redrawNode = function(node){
	this.board.removeChild(node.htmlNode);
	if(node.board) {
		node.board = null;
	}
	if(node.typ=="Info") {
		var infoTxt = node.edge.getInfo(node.node);
		node.edge.addElement(this.board, this.drawer.createInfo(node, false, infoTxt));
	}else{
		node.htmlNode = this.drawer.getNode(node);
		if(node.htmlNode){
			this.board.appendChild( node.htmlNode );
		}
	}
	node.center = new Pos(node.x + (node.width / 2), node.y + (node.height / 2));
	node._parent.resize(this.model);
}
Graph.prototype.getGraphNode = function(objElement){
	var obj=objElement;
	while(obj&&!obj.node){
		if(!obj.node){
			obj=obj.parentNode;
		}else{
			break;
		}
	}
	if(obj&&obj.node){
		return obj.node;
	}
	return null;
}
Graph.prototype.initDrawer = function(typ){
	typ = typ.toLowerCase();
	if(this.model.options.display==typ) {
		return;
	}
	this.clearBoard();
	model.htmlNode = this.drawer.createContainer(this);
	this.initDragAndDrop();
	this.root.appendChild(this.board);
	this.model.options.display = typ;
	if(typ=="html"){
		this.drawer = new HTMLDrawer();
	}else if(typ=="svg"){
		this.drawer = new SVGDrawer();
	}else if(typ=="canvas"){
		this.drawer = new CanvasDrawer();
	}
}
Graph.prototype.setTyp = function(typ){
	if(typ=="HTML"){
		this.initDrawer(typ);
		this.loader.init(true);
		this.initGraph(this.model);
		this.drawGraph(this.model);
	}else if(typ=="SVG"){
		this.initDrawer(typ);
		this.initGraph(this.model);
		this.drawGraph(this.model);
	}else if(typ=="PNG"){
		var oldDrawer = this.drawer;
		this.drawer = new CanvasDrawer();
		this.loader.init(false);
		this.loader.oldDrawer = oldDrawer;
		this.initGraph(this.model);
		this.drawGraph(this.model);
		this.loader.resetDrawer();
	}else if(typ=="PDF"){
		this.ExportPDF();
	}
}
Graph.prototype.serializeXmlNode = function(xmlNode) {
	if (typeof window.XMLSerializer != "undefined") {
		return (new window.XMLSerializer()).serializeToString(xmlNode);
	} else if (typeof xmlNode.xml != "undefined") {
		return xmlNode.xml;
	}
	return xmlNode.outerHTML;
}
Graph.prototype.utf8_to_b64 = function( str ) {
	return window.btoa(unescape(encodeURIComponent( str )));
}
Graph.prototype.ExportPDF = function () {
	var pdf = new jsPDF('l', 'px', 'a4');
	new svgConverter(this.board, pdf, {removeInvalid: false});
	pdf.save('Download.pdf');
}
Graph.prototype.ExportEPS = function () {
	var doc = new jsEPS({inverting:true});
	var converter = new svgConverter(this.board, doc);
	doc.save();
}
Graph.prototype.ExportPNG = function () {
	var image = new Image();
	image.src = 'data:image/svg+xml;base64,' + this.utf8_to_b64(this.serializeXmlNode(this.board));
	var that = this;
	image.onload = function(e) {
		var canvas = document.createElement('canvas');
		canvas.width = image.width;
		canvas.height = image.height;
		var context = canvas.getContext('2d');
		context.drawImage(image, 0, 0);
		var a = document.createElement('a');
		a.download = "download.png";
		a.href = canvas.toDataURL('image/png');
		a.click();
	};
}
Graph.prototype.SaveAs = function (typ) {
	typ = typ.toLowerCase();
	if(typ=="svg") {
		this.Save("image/svg+xml", this.serializeXmlNode(this.board), "download.svg");
	}else if(typ=="html") {
		this.ExportHTML();
	}else if(typ=="htmlx") {
		this.ExportHTMLStandalone();
	}else if(typ=="png") {
		this.ExportPNG();
	}else if(typ=="pdf") {
		this.ExportPDF();
	}else if(typ=="eps") {
		this.ExportEPS();
	}
}
Graph.prototype.Save = function (typ, data, name) {
	var a = document.createElement("a");
	var url = window.URL.createObjectURL(new Blob([data], {type: typ}));
	a.href = url;
	a.download = name;
	a.click();
}
Graph.prototype.ExportHTML = function () {
	var json = this.model.toJson();
	var data="<html><head>"+document.head.innerHTML.trim()+"</head><body><script>"
		+"new Graph("+JSON.stringify(json, null, "\t") +").layout();</script></body></html>";
	this.Save("text/json", data, "download.html");
}
Graph.prototype.ExportHTMLStandalone = function () {
	var json = this.model.toJson();

	var data="<html><head>"+document.head.innerHTML.trim()+"</head><body><script>"
		+"new Graph("+JSON.stringify(json, null, "\t") +").layout();</script></body></html>";
	this.Save("text/json", data, "download.html");
}




































//				######################################################### GraphLayout-Dagre #########################################################
DagreLayout = function() {}
DagreLayout.prototype.layout = function(graph, node, width, height) {
	var g = new dagre.graphlib.Graph(node.merge({directed:false}, node.options.layout));
	g.setGraph({});
	g.setDefaultEdgeLabel(function() { return {}; });
	for (var i in node.nodes) {
		var n = node.nodes[i];
		g.setNode(n.id, {label: n.id, width:n.width, height:n.height, x:n.x, y:n.y});
	}
	for (var i = 0; i < node.edges.length; i++) {
		var e = node.edges[i];
		g.setEdge(this.getRootNode(e.source).id, this.getRootNode(e.target).id);
	}

	dagre.layout(g);
	// Set the layouting back
	for (var i in node.nodes) {
		var n = node.nodes[i];
		var layoutNode = g.node(n.id);
		n.x = layoutNode.x - (n.width/2);
		n.y = layoutNode.y - (n.height/2);
	}
	graph.draw(node, width, height);
}
DagreLayout.prototype.getRootNode = function(node, child) {
	if(node._parent){
		return this.getRootNode(node._parent, node);
	}
	if(!child){
		return node;
	}
	return child;
}
//				######################################################### Loader #########################################################
Loader = function(graph) {this.init(false);this.graph=graph;}
Loader.prototype.init = function(abort){
	this.images = [];
	this.abort=abort;
}
Loader.prototype.resetDrawer = function(){
	if(this.images.length==0){
		this.graph.drawer.onFinishImage();
	}else{
		var img = this.images.pop();
		this.graph.root.appendChild(img);
	}
}
Loader.prototype.remove = function(img){this.images.remove(img);}
Loader.prototype.onLoad = function(img){
	var idx = this.images.indexOf(img);
	if (idx != -1) {
		this.images.splice(idx, 1);
	}
	if(this.images.length==0){
		this.graph.drawer.onFinishImage();
	}
}
Loader.prototype.appendImg = function(img){
	img.crossOrigin = 'anonymous';
	var that = this.graph.drawer;
	img.onload = function(e){that.onLoadImage(e);};
	this.images.push(img);
}
//				######################################################### LINES #########################################################
Edge = function() {this.init();this.typ="EDGE";}
Edge.prototype.init = function(){
	this.path = [];
	this.source=null;
	this.target=null;
	this.start = null;
	this.end = null;
	this.htmlElement = new Array();
	this.m = 0;
	this.n = 0;
	this.lineStyle = Line.Format.SOLID;
}
Edge.Layout ={ DIAG : 1, RECT : 0 }
Edge.Position={UP:"UP", LEFT:"LEFT", RIGHT:"RIGHT", DOWN:"DOWN"}
Edge.prototype.addElement = function(board, element){
	if(element){
		this.htmlElement.push(element);board.appendChild(element);
	}
}
Edge.prototype.removeElement = function(element){
	if(element){
		for(var i=0;i<this.htmlElement.length;i++){
			if(this.htmlElement[i]==element){
				this.htmlElement.splice(i, 1);
				i--;
			}
		}
	}
}
Edge.prototype.calculate = function(board, drawer){
	this.source.center = new Pos(this.source.getX() + (this.source.width / 2), this.source.getY() + (this.source.height / 2));
	this.target.center = new Pos(this.target.getX() + (this.target.width / 2), this.target.getY() + (this.target.height / 2));
	return this.calcCenterLine();
}//FIXME
Edge.prototype.draw = function(board, drawer){
	for(var i=0;i<this.path.length;i++){
		var p = this.path[i];
		this.addElement(board, drawer.createLine(p.source.x, p.source.y, p.target.x, p.target.y, p.line, p.style));
	}
	var options = drawer.model.options;
	this.drawSourceText(board, drawer, options);
	if(this.info) {
		var angle = Math.atan((p.source.y-p.target.y)/(p.source.x-p.target.x))*60;
		this.addElement(board, drawer.createInfo(this.infoPos, false, this.info, angle));
		this.addElement(board, new SymbolLibary().create({typ:"Arrow",x:this.infoPos.x,y:this.infoPos.y, rotate:angle}, drawer));
	}
	this.drawTargetText(board, drawer, options);
}
Edge.prototype.drawSourceText = function(board, drawer, options){
	var infoTxt = this.getInfo(this.sourceInfo);
	if(infoTxt.length > 0 ){
		this.addElement(board, drawer.createInfo(this.sourceInfo, false, infoTxt));
	}
}
Edge.prototype.drawTargetText = function(board, drawer, options){
	var infoTxt = this.getInfo(this.targetInfo);
	if(infoTxt.length > 0 ){
		this.addElement(board, drawer.createInfo(this.targetInfo, false, infoTxt));
	}
}
Edge.prototype.endPos = function(){return this.path[this.path.length-1];}
Edge.prototype.edgePosition = function() {
	var pos=0;
	for(var i=0;i < this.source.edges.length; i++) {
		if(this.source.edges[i] == this){
			return pos;
		}
		if(this.source.edges[i].target == this.target){
			pos ++;
		}
	}
	return pos;
}
Edge.prototype.getTarget = function(node, startNode){
	if(node instanceof GraphModel){
		if(node.status=="close") {
			return node;
		}
		return startNode;
	}
	return this.getTarget(node._parent, startNode);
}
Edge.prototype.calcCenterLine = function(){
	var divisor = (this.target.center.x - this.source.center.x);
	var sourcePos,targetPos;
	var edgePos = this.edgePosition() * 20;

	this.path = new Array();
	var source = this.getTarget(this.source, this.source), target = this.getTarget(this.target, this.target);
	if(divisor==0){
		if(this.source==this.target){
			/* OwnAssoc */
			return false;
		}
		// Must be UP_DOWN or DOWN_UP
		if(this.source.center.y<this.target.center.y){
			// UP_DOWN
			sourcePos = this.getCenterPosition(source, Edge.Position.DOWN, edgePos);
			targetPos = this.getCenterPosition(target, Edge.Position.UP, edgePos);
		}else{
			sourcePos = this.getCenterPosition(source, Edge.Position.UP, edgePos);
			targetPos = this.getCenterPosition(target, Edge.Position.DOWN, edgePos);
		}
	}else{
		this.m = (target.center.y - source.center.y) / divisor;
		this.n = source.center.y - (source.center.x * this.m);
		sourcePos = this.getPosition(this.m,this.n, source, target.center, edgePos);
		targetPos = this.getPosition(this.m,this.n, target, sourcePos, edgePos);
	}
	if(sourcePos&&targetPos){
		this.calcInfoPos( sourcePos, source, this.sourceInfo, edgePos);
		this.calcInfoPos( targetPos, target, this.targetInfo, edgePos);
		this.addEdgeToNode(source, sourcePos.id);
		this.addEdgeToNode(target, targetPos.id);
		this.path.push ( new Line(sourcePos, targetPos, this.lineStyle, this.style));
		this.infoPos = new Pos( (sourcePos.x + targetPos.x)/2, (sourcePos.y + targetPos.y)/2 );
	}
	return true;
}
Edge.prototype.getCenterPosition = function(node, pos, offset){
	offset = offset || 0;
	if(pos==Edge.Position.DOWN){
		return new Pos(node.center.x + offset, (node.y+node.height), Edge.Position.DOWN);
	}
	if(pos==Edge.Position.UP){
		return new Pos(node.center.x + offset, node.y, Edge.Position.UP);
	}
	if(pos==Edge.Position.LEFT){
		return new Pos(node.x, node.center.y + offset, Edge.Position.LEFT);
	}
	if(pos==Edge.Position.RIGHT){
		return new Pos(node.x+node.width, node.center.y + offset, Edge.Position.RIGHT);
	}
}
Edge.prototype.getInfo = function(info){
	var infoTxt = "";
	var isCardinality = this.model.typ=="classdiagram" && this.model.options.CardinalityInfo;
	var isProperty = this.model.typ=="classdiagram" && this.model.options.PropertyInfo;

	if(isProperty && info.property){
		infoTxt = info.property;
	}
	if(isCardinality && info.cardinality){
		if(infoTxt.length > 0 ){
			infoTxt += "\n";
		}
		if(info.cardinality.toLowerCase() == "one"){
			infoTxt += "0..1";
		}else if(info.cardinality.toLowerCase() == "many"){
			infoTxt += "0..*";
		}
	}
	return infoTxt;
}
Edge.prototype.calcOwnEdge = function(){
	//this.source
	var offset = 20;
	this.start = this.getFree(this.source);
	if(this.start.length>0){
		this.end = this.getFreeOwn(this.source, this.start);
	}else{
		this.start = Edge.Position.RIGHT;
		this.end = Edge.Position.DOWN;
	}

	var sPos = this.getCenterPosition(this.source, this.start);
	var tPos;
	if(this.start==Edge.Position.UP){
		tPos = new Pos(sPos.x, sPos.y - offset);
	}else if(this.start==Edge.Position.DOWN){
		tPos = new Pos(sPos.x, sPos.y + offset);
	}else if(this.start==Edge.Position.RIGHT){
		tPos = new Pos(sPos.x + offset, sPos.y);
	}else if(this.start==Edge.Position.LEFT){
		tPos = new Pos(sPos.x - offset, sPos.y);
	}
	this.path.push (new Line(sPos, tPos, this.lineStyle));
	if(this.end==Edge.Position.LEFT || this.end==Edge.Position.RIGHT){
		if(this.start==Edge.Position.LEFT){
			sPos = tPos;
			tPos = new Pos(sPos.x, this.source.y - offset);
			this.path.push (new Line(sPos, tPos, this.lineStyle));
		}else if(this.start==Edge.Position.RIGHT){
			sPos = tPos;
			tPos = new Pos(sPos.x, this.source.y + offset);
			this.path.push (new Line(sPos, tPos, this.lineStyle));
		}
		sPos = tPos;
		if(this.end==Edge.Position.LEFT){
			tPos = new Pos(this.source.x - offset, sPos.y);
		}else{
			tPos = new Pos(this.source.x + this.source.width + offset, sPos.y);
		}
		this.path.push (new Line(sPos, tPos, this.lineStyle));
		sPos = tPos;
		tPos = new Pos(sPos.x, this.source.center.y);
		this.path.push (new Line(sPos, tPos, this.lineStyle));

		this.infoPos = new Pos( (sPos.x + tPos.x)/2, sPos.y );
	}else if(this.end==Edge.Position.UP || this.end==Edge.Position.DOWN){
		if(this.start==Edge.Position.UP){
			sPos = tPos;
			tPos = new Pos(this.source.x + this.source.width + offset, sPos.y);
			this.path.push (new Line(sPos, tPos, this.lineStyle));
		}else if(this.start==Edge.Position.DOWN){
			sPos = tPos;
			tPos = new Pos(this.source.x - offset, sPos.y);
			this.path.push (new Line(sPos, tPos, this.lineStyle));
		}
		sPos = tPos;
		if(this.end==Edge.Position.UP){
			tPos = new Pos(sPos.x, this.source.y - offset);
		}else{
			tPos = new Pos(sPos.x, this.source.y + this.source.height + offset);
		}
		this.path.push (new Line(sPos, tPos, this.lineStyle));
		sPos = tPos;
		tPos = new Pos(this.source.center.x, sPos.y);
		this.path.push (new Line(sPos, tPos, this.lineStyle));

		this.infoPos = new Pos( sPos.x , (sPos.y + tPos.y)/2 );
	}
	sPos = tPos;
	this.path.push (new Line(sPos, this.getCenterPosition(this.source, this.end), this.lineStyle));
}
Edge.prototype.addEdgeToNode=function(node, pos){
	if(pos==Edge.Position.UP){
		node.UP+=1;
	}else if(pos==Edge.Position.DOWN){
		node.DOWN+=1;
	}else if(pos==Edge.Position.RIGHT){
		node.RIGHT+=1;
	}else if(pos==Edge.Position.LEFT){
		node.LEFT+=1;
	}
}
Edge.prototype.getFree = function(node){
	if(node.UP==0 ){
		node.UP +=1;
		return Edge.Position.UP;
	}else if(node.RIGHT==0 ){
		node.RIGHT +=1;
		return Edge.Position.RIGHT;
	}else if(node.DOWN==0 ){
		node.DOWN +=1;
		return Edge.Position.DOWN;
	}else if(node.LEFT==0 ){
		node.LEFT +=1;
		return Edge.Position.LEFT;
	}
	return "";
}
Edge.prototype.getFreeOwn = function(node, start){
	var list = new Array(Edge.Position.UP, Edge.Position.RIGHT, Edge.Position.DOWN, 
		Edge.Position.LEFT, Edge.Position.UP, Edge.Position.RIGHT, Edge.Position.DOWN);
	var result = new Array();
	var id=0;
	for(var i=0;i<list.length;i++) {
		if(list[i]==start) {
			id =i;
			break;
		}
	}
	if(node[list[id + 1]] == 0 || node[list[id + 1]] < node[list[id + 3]]) {
		node[list[id + 1]] ++;
		return list[id + 1];
	}
	node[list[id + 3]]++;
	return list[id + 3];
}
Edge.prototype.calcInfoPos = function(linePos, item, info, offset){
	// Manuell move the InfoTag
	offset = offset || 0;
	var spaceA = 20;
	var spaceB = 10;
	if(info.custom){
		return;
	}
	var newY = linePos.y;
	var newX = linePos.x;
	var yoffset = 0;
	if(linePos.id==Edge.Position.UP){
		newY = newY - info.height - offset - spaceA;
		if(this.m!=0){
			newX = (newY-this.n) / this.m + spaceB;
		}
	}else if(linePos.id==Edge.Position.DOWN){
		newY = newY + offset + spaceA;
		if(this.m!=0){
			newX = (newY-this.n) / this.m + spaceB;
		}
	}else if(linePos.id==Edge.Position.LEFT){
		newX = newX - info.width - offset - spaceA;
		if(this.m!=0){
			newY = (this.m * newX)+ this.n;
		}
	}else if(linePos.id==Edge.Position.RIGHT){
		newX += offset + spaceA;
		if(this.m!=0){
			newY = (this.m * newX)+ this.n;
		}
	}
	info.id = linePos.id;
	info.x = newX;
	info.y = newY;
}
Edge.prototype.getPosition= function(m , n, entity, refCenter, offset){
	if (!offset) {
		offset = 0;
	}
	var x,y;
	var pos=new Array();
	var distance=new Array();
	x = entity.getX()+entity.width;
	y = m*x+n;
	if(y>=entity.getY() && y<=(entity.getY()+entity.height)){
		pos.push(new Pos(x , y + offset, Edge.Position.RIGHT));
		distance.push(Math.sqrt((refCenter.x-x)*(refCenter.x-x)+(refCenter.y-y)*(refCenter.y-y)));
	}
	y = entity.getY();
	x = (y-n)/m;
	if(x>=entity.getX() && x<=(entity.getX()+entity.width)){
		pos.push(new Pos(x + offset, y, Edge.Position.UP));
		distance.push(Math.sqrt((refCenter.x-x)*(refCenter.x-x)+(refCenter.y-y)*(refCenter.y-y)));
	}
	x = entity.getX();
	y = m*x+n;
	if(y>=entity.getY() && y<=(entity.getY()+entity.height)){
		pos.push(new Pos(x , y + offset, Edge.Position.LEFT));
		distance.push(Math.sqrt((refCenter.x-x)*(refCenter.x-x)+(refCenter.y-y)*(refCenter.y-y)));
	}
	y = entity.getY()+entity.height;
	x = (y-n)/m;
	if(x>=entity.getX() && x<=(entity.getX()+entity.width)){
		pos.push(new Pos(x + offset, y, Edge.Position.DOWN));
		distance.push(Math.sqrt((refCenter.x-x)*(refCenter.x-x)+(refCenter.y-y)*(refCenter.y-y)));
	}
	var min=999999999;
	var position;
	for(var i=0;i<pos.length;i++){
		if(distance[i]<min){
			 min = distance[i];
			 position = pos[i];
		}
	}
	return position;
}
Edge.prototype.calcMoveLine = function(size, angle, move){
	var startArrow	= this.endPos().source;
	this.end = this.endPos().target;
	// calculate the angle of the line
	var lineangle=Math.atan2(this.end.y-startArrow.y,this.end.x-startArrow.x);
	// h is the line length of a side of the arrow head
	var h=Math.abs(size/Math.cos(angle));
	var angle1=lineangle+Math.PI+angle;
	var hCenter=Math.abs((size/2)/Math.cos(angle));

	this.top = new Pos(this.end.x+Math.cos(angle1)*h, this.end.y+Math.sin(angle1)*h);
	this.topCenter = new Pos(this.end.x+Math.cos(angle1)*hCenter, this.end.y+Math.sin(angle1)*hCenter);
	var angle2=lineangle+Math.PI-angle;
	this.bot = new Pos(this.end.x+Math.cos(angle2)*h, this.end.y+Math.sin(angle2)*h);
	this.botCenter = new Pos(this.end.x+Math.cos(angle2)*hCenter, this.end.y+Math.sin(angle2)*hCenter);
	if(move) {
		this.endPos().target = new Pos((this.top.x + this.bot.x) / 2, (this.top.y + this.bot.y) / 2);
	}
}
Generalization = function() { this.init();this.typ="Generalization";}
Generalization.prototype = Object_create(Edge.prototype);
Generalization.prototype.calculateEdge = Generalization.prototype.calculate;
Generalization.prototype.calculate = function(board, drawer){
	if(!this.calculateEdge(board, drawer)){
		return false;
	}
	this.calcMoveLine(16, 50, true);
	return true;
}
Generalization.prototype.drawSuper = Generalization.prototype.draw;
Generalization.prototype.draw = function(board, drawer){
	this.drawSuper(board, drawer);
	this.addElement(board, drawer.createLine(this.top.x, this.top.y, this.end.x, this.end.y, this.lineStyle));
	this.addElement(board, drawer.createLine(this.bot.x, this.bot.y, this.end.x, this.end.y, this.lineStyle));
	this.addElement(board, drawer.createLine(this.top.x, this.top.y, this.bot.x, this.bot.y, this.lineStyle));
}
Generalization.prototype.drawSourceText = function(board, drawer, options){}
Generalization.prototype.drawTargetText = function(board, drawer, options){}

Implements = function() { this.init();this.typ="Implements";this.lineStyle = Line.Format.DOTTED;}
Implements.prototype = Object_create(Generalization.prototype);

Unidirectional = function() { this.init();this.typ="Unidirectional";}
Unidirectional.prototype = Object_create(Generalization.prototype);
Unidirectional.prototype.calculate = function(board, drawer){
	if(!this.calculateEdge(board, drawer)){
		return false;
	}
	this.calcMoveLine(16, 50, false);
	return true;
}
Unidirectional.prototype.draw = function(board, drawer){
	this.drawSuper(board, drawer);
	this.addElement(board, drawer.createLine(this.top.x, this.top.y, this.end.x, this.end.y, this.lineStyle));
	this.addElement(board, drawer.createLine(this.bot.x, this.bot.y, this.end.x, this.end.y, this.lineStyle));
}
Aggregation = function() { this.init();this.typ="Aggregation";}
Aggregation.prototype = Object_create(Generalization.prototype);
Aggregation.prototype.calculate = function(board, drawer){
	if(!this.calculateEdge(board, drawer)){
		return false;
	}
	this.size=16;
	this.calcMoveLine(this.size, 49.8, true);
	return true;
}
Aggregation.prototype.draw = function(board, drawer){
	this.drawSuper(board, drawer);
	this.addElement(board, drawer.createPath(true, "none", [this.endPos().target, this.topCenter, this.end, this.botCenter]));
}
Composition = function() { this.init();this.typ="Composition";}
Composition.prototype = Object_create(Aggregation.prototype);
Composition.prototype.draw = function(board, drawer){
	this.drawSuper(board, drawer);

	var start = this.endPos().source;
	var end = this.endPos().target;
	var a = (start.y - end.y) / (end.x - start.x);
	var h = Math.atan(a)*100;

	this.addElement(board, drawer.createPath(true, "#000", [this.endPos().target, this.topCenter, this.end, this.botCenter], h));
}
function RGBColor(value){
	this.ok = false;
	if(value=="none"){
		return;
	}
	var div = document.createElement("div");
	div.style.backgroundColor = value;
	document.body.appendChild(div);
	var computedColor = window.getComputedStyle(div).backgroundColor;
	// cleanup temporary div.
	document.body.removeChild(div);
	this.convert(computedColor);
}
RGBColor.prototype.convert = function(value){
	var regex = /rgb *\( *([0-9]{1,3}) *, *([0-9]{1,3}) *, *([0-9]{1,3}) *\)/;
	var values = regex.exec(value);
	this.r = parseInt(values[1]);
	this.g = parseInt(values[2]);
	this.b = parseInt(values[3]);
	this.ok = true;
}
RGBColor.prototype.toRGB = function () {return 'rgb(' + this.r + ', ' + this.g + ', ' + this.b + ')';}
RGBColor.prototype.toHex = function () {
	return "#" 
	+ (this.r + 0x10000).toString(16).substring(3).toUpperCase() 
	+ (this.g + 0x10000).toString(16).substring(3).toUpperCase()
	+ (this.b + 0x10000).toString(16).substring(3).toUpperCase();
}
