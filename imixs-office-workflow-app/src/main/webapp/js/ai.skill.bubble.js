"use strict";

// define script name spaces
IMIXS.namespace("org.imixs.workflow.skillbubble");

// tree data - provided by SkillController.getSkillTreeJson() via skill-bubble.xhtml
var data = (typeof imixsSkillTreeData !== "undefined") ? imixsSkillTreeData : [];

// layout configuration
const LEAF_R = 34, PAD = 8;
const FOCUS_PADDING = 2.75;
const SIBLING_GAP = 46;
const SINGLE_CHILD_SCALE = 0.5;
const ZOOM_DURATION_MS = 360;
const COLORS = ['c-teal', 'c-purple', 'c-coral', 'c-amber', 'c-pink'];

// internal state, populated on DOMContentLoaded
var root;
var allNodes;
var zoomG;
var currentView;
var currentFocus;
var minViewR, maxViewR;
var zoomToken = 0;
var actionsFO;
var actionsTargetNode = null;
var hideActionsTimer = null;
var currentK = 1, currentTx = 0, currentTy = 0;

/**
 * Init method for the skill bubble page.
 * Builds the tree layout, renders the SVG nodes and sets the initial view.
 */
document.addEventListener("DOMContentLoaded", function () {
    root = { name: '', children: data };
    prepare(root, null, -1);
    layout(root);
    assignAbs(root, 0, 0);

    // zoom bounds: don't allow zooming in past a single leaf, or out into empty space
    minViewR = LEAF_R * 0.6;
    maxViewR = root.r * 1.4;

    allNodes = flatten(root, []);
    allNodes.sort(function (a, b) { return a.depth - b.depth; });

    zoomG = document.getElementById('zoomG');
    renderNodes();

    currentView = { x: 0, y: 0, r: root.r * 1.05 };
    currentFocus = root;
    applyView(currentView);
    updatePath();

    document.getElementById('pack-svg').addEventListener('wheel', handleWheelZoom, { passive: false });
});

/**
 * Recursively attaches parent references and depth to each node.
 */
function prepare(node, parent, depth) {
    node.parent = parent;
    node.depth = depth;
    if (node.children) {
        node.children.forEach(function (c) {
            prepare(c, node, depth + 1);
        });
    }
}

/**
 * Free, map-like zoom around the mouse pointer, independent of the tree hierarchy.
 * Cancels any running click-triggered zoom animation via the zoomToken guard.
 */
function handleWheelZoom(e) {
    e.preventDefault();
    zoomToken++;

    var svg = document.getElementById('pack-svg');
    var rect = svg.getBoundingClientRect();
    var viewBoxSize = 640;
    var pxX = e.clientX - rect.left;
    var pxY = e.clientY - rect.top;
    var viewBoxX = pxX * (viewBoxSize / rect.width);
    var viewBoxY = pxY * (viewBoxSize / rect.height);

    var kOld = 300 / currentView.r;
    var dataX = currentView.x + (viewBoxX - 320) / kOld;
    var dataY = currentView.y + (viewBoxY - 320) / kOld;

    var factor = e.deltaY < 0 ? 0.9 : 1.1;
    var newR = Math.max(minViewR, Math.min(maxViewR, currentView.r * factor));

    var kNew = 300 / newR;
    var newX = dataX - (viewBoxX - 320) / kNew;
    var newY = dataY - (viewBoxY - 320) / kNew;

    currentView = { x: newX, y: newY, r: newR };
    applyView(currentView);
}

/**
 * Animates from the current view to a target view over ZOOM_DURATION_MS.
 * Uses a token guard so a newer zoom (click or wheel) cancels an older animation.
 */
function animateTo(target, focusNodeRef) {
    var myToken = ++zoomToken;
    var from = currentView;
    var start = performance.now();
    var duration = ZOOM_DURATION_MS;
    function frame(now) {
        if (myToken !== zoomToken) return; // a wheel-zoom or newer click-zoom took over
        var t = Math.min(1, (now - start) / duration);
        var e = easeInOutCubic(t);
        applyView({
            x: from.x + (target.x - from.x) * e,
            y: from.y + (target.y - from.y) * e,
            r: from.r + (target.r - from.r) * e
        });
        if (t < 1) {
            requestAnimationFrame(frame);
        } else {
            currentView = target;
            currentFocus = focusNodeRef;
            updatePath();
        }
    }
    requestAnimationFrame(frame);
}

/**
 * Places children so adjacent circles are exactly tangent (no artificial padding factor),
 * and alternates large/small radii around the ring to minimize wasted space.
 */
function packChildren(node) {
    var kids = node.children;
    var n = kids.length;
    if (n === 1) {
        var child = kids[0];
        var originalR = child.r;
        scaleSubtree(child, SINGLE_CHILD_SCALE);
        // place the shrunk child at the 6 o'clock position, using the space
        // freed up by shrinking so the parent's own radius stays unchanged
        var offset = originalR * (1 - SINGLE_CHILD_SCALE);
        child.x = 0;
        child.y = offset;
        node.r = originalR + PAD;
        return;
    }
    var sorted = kids.slice().sort(function (a, b) { return b.r - a.r; });
    var arranged = new Array(n);
    var lo = 0, hi = n - 1, useLo = true;
    for (var i = 0; i < n; i++) {
        arranged[i] = useLo ? sorted[lo++] : sorted[hi--];
        useLo = !useLo;
    }
    var angleStep = (2 * Math.PI) / n;
    var maxPairSum = 0;
    for (var j = 0; j < n; j++) {
        var a = arranged[j], b = arranged[(j + 1) % n];
        maxPairSum = Math.max(maxPairSum, a.r + b.r + SIBLING_GAP);
    }
    var ringR = maxPairSum / (2 * Math.sin(angleStep / 2));
    arranged.forEach(function (k, idx) {
        var angle = idx * angleStep - Math.PI / 2;
        k.x = ringR * Math.cos(angle);
        k.y = ringR * Math.sin(angle);
    });
    var maxR = Math.max.apply(null, kids.map(function (k) { return k.r; }));
    node.r = ringR + maxR + PAD;
}

/**
 * Recursively scales a node's radius and, for every descendant, both its
 * relative position and radius by the given factor. Used to shrink a lone
 * child so it visually reads as clearly smaller than its parent.
 */
function scaleSubtree(node, factor) {
    node.r *= factor;
    if (node.children) {
        node.children.forEach(function (c) {
            c.x *= factor;
            c.y *= factor;
            scaleSubtree(c, factor);
        });
    }
}
/**
 * Recursively computes the radius of every node, bottom-up.
 */
function layout(node) {
    if (node.children && node.children.length) {
        node.children.forEach(layout);
        packChildren(node);
    } else {
        node.r = LEAF_R;
    }
}

/**
 * Converts relative child positions into absolute canvas coordinates.
 */
function assignAbs(node, px, py) {
    node.cx = px + (node.x || 0);
    node.cy = py + (node.y || 0);
    if (node.children) {
        node.children.forEach(function (c) {
            assignAbs(c, node.cx, node.cy);
        });
    }
}

/**
 * Flattens the tree (excluding the invisible super-root) into a single array.
 */
function flatten(node, out) {
    if (node.depth >= 0) out.push(node);
    if (node.children) {
        node.children.forEach(function (c) {
            flatten(c, out);
        });
    }
    return out;
}

/**
 * Renders all circles into one layer and all labels into a separate layer
 * on top, so labels are never visually covered by another node's circle.
 * Also creates a single, shared foreignObject with edit / add-sub-skill icons
 * that gets repositioned to whichever node currently has focus.
 */
function renderNodes() {
    var NS = 'http://www.w3.org/2000/svg';
    var circlesLayer = document.createElementNS(NS, 'g');
    var labelsLayer = document.createElementNS(NS, 'g');
    labelsLayer.style.pointerEvents = 'none';
    zoomG.appendChild(circlesLayer);
    zoomG.appendChild(labelsLayer);

    allNodes.forEach(function (node) {
        var idx = allNodes.indexOf(node);
        var colorClass = COLORS[Math.min(node.depth, COLORS.length - 1)];

        var g = document.createElementNS(NS, 'g');
        g.setAttribute('class', 'pack-node ' + colorClass);
        g.setAttribute('onclick', 'event.stopPropagation(); focusNode(' + idx + ')');
        var circle = document.createElementNS(NS, 'circle');
        circle.setAttribute('cx', node.cx);
        circle.setAttribute('cy', node.cy);
        circle.setAttribute('r', node.r);
        circle.setAttribute('stroke-width', '0.5');
        g.appendChild(circle);

        // toolbar
        g.setAttribute('onclick', 'event.stopPropagation(); focusNode(' + idx + ')');
        g.addEventListener('mouseenter', function () { showActionsFor(node); });
        g.addEventListener('mouseleave', scheduleHideActions);

        circlesLayer.appendChild(g);

        var labelG = document.createElementNS(NS, 'g');
        labelG.setAttribute('class', 'pack-node ' + colorClass);
        var text = document.createElementNS(NS, 'text');
        text.setAttribute('x', node.cx);
        text.setAttribute('y', node.cy);
        text.setAttribute('text-anchor', 'middle');
        text.setAttribute('dominant-baseline', 'central');
        text.setAttribute('class', node.children ? 'th' : 't');
        text.textContent = node.name;
        labelG.appendChild(text);
        labelsLayer.appendChild(labelG);

        // measure the rendered text width once, so the action icons can be
        // placed exactly after the label instead of at a fixed offset
        node.labelWidth = text.getComputedTextLength();

        node.el = g;
    });

    // shared action-icon element, repositioned to whichever node is hovered.
    // Appended to the <svg> itself (not zoomG) so it stays a constant screen
    // size regardless of the current zoom level.
    actionsFO = document.createElementNS(NS, 'foreignObject');
    actionsFO.setAttribute('width', 40);
    actionsFO.setAttribute('height', 18);
    actionsFO.style.display = 'none';
    var actionsDiv = document.createElement('div');
    actionsDiv.className = 'pack-actions';
    actionsDiv.innerHTML =
        '<a href="#" onclick="editFocusedSkill(); return false;" title="Edit">' +
            '<i class="fa-solid fa-pen"></i></a>' +
        '<a href="#" onclick="addSubSkillToFocused(); return false;" title="Add sub-skill">' +
            '<i class="fa-solid fa-diagram-project"></i></a>';
    actionsDiv.addEventListener('mouseenter', function () { clearTimeout(hideActionsTimer); });
    actionsDiv.addEventListener('mouseleave', scheduleHideActions);
    actionsFO.appendChild(actionsDiv);
    document.getElementById('pack-svg').appendChild(actionsFO);
   // zoomG.appendChild(actionsFO);
}

/**
 * Applies the current view (center + radius) as a transform on the zoom group.
 */
function applyView(v) {
    var k = 300 / v.r;
    var tx = 320 - v.x * k;
    var ty = 320 - v.y * k;
    zoomG.setAttribute('transform', 'translate(' + tx + ',' + ty + ') scale(' + k + ')');
    currentK = k; currentTx = tx; currentTy = ty;
    if (actionsTargetNode) positionActionsFO(actionsTargetNode);
}

/**
 * Converts a node's data-space center into current screen-space coordinates
 * and places the (constant-size) action icons just to the right of its label.
 */
function positionActionsFO(node) {
    var screenX = currentTx + node.cx * currentK;
    var screenY = currentTy + node.cy * currentK;
    var halfLabelWidth = (node.labelWidth || 0) / 2 * currentK;
    actionsFO.setAttribute('x', screenX + halfLabelWidth + 6);
    actionsFO.setAttribute('y', screenY - 9);
}

/**
 * Shows the edit / add-sub-skill icons under the given node, canceling any pending hide.
 */
function showActionsFor(node) {
    clearTimeout(hideActionsTimer);
    actionsTargetNode = node;
    positionActionsFO(node);
    actionsFO.style.display = 'block';
}

/**
 * Hides the action icons after a short delay, giving the mouse time to
 * move from the bubble onto the icons themselves without flickering.
 */
function scheduleHideActions() {
    clearTimeout(hideActionsTimer);
    hideActionsTimer = setTimeout(function () {
        actionsFO.style.display = 'none';
        actionsTargetNode = null;
    }, 150);
}

/**
 * Cubic ease-in-out, used for the zoom transition.
 */
function easeInOutCubic(t) {
    return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
}

/**
 * Updates the breadcrumb label above the canvas and repositions the
 * edit / add-sub-skill icons under the currently focused node.
 */
function updatePath() {
    var names = [];
    var n = currentFocus;
    while (n && n.depth >= 0) {
        names.unshift(n.name);
        n = n.parent;
    }
    document.getElementById('pack-path').textContent = names.length ? 'Root > ' + names.join(' > ') : 'Root';
}

/**
 * Zooms in on the clicked node. Called from the inline onclick handler.
 */
function focusNode(idx) {
    var node = allNodes[idx];
    if (node === currentFocus) return;
    animateTo({ x: node.cx, y: node.cy, r: node.r * FOCUS_PADDING }, node);
}

/**
 * Zooms out to the parent of the current focus node.
 */
function zoomOut() {
    if (currentFocus === root) return;
    var parent = currentFocus.parent;
    if (parent === root) {
        animateTo({ x: 0, y: 0, r: root.r * 1.05 }, root);
    } else {
        animateTo({ x: parent.cx, y: parent.cy, r: parent.r * FOCUS_PADDING }, parent);
    }
}

/**
 * Resets the view to show the full tree.
 */
function resetPack() {
    animateTo({ x: 0, y: 0, r: root.r * 1.05 }, root);
}

/**
 * Navigates to the edit page of the currently focused skill.
 */
function editFocusedSkill() {
    if (!actionsTargetNode || !actionsTargetNode.id) return;
    document.location.href = imixsOfficeMain.contextPath + "/pages/admin/skill.jsf?id=" + actionsTargetNode.id;
}

/**
 * Navigates to the create page for a new sub-skill under the currently focused skill.
 */
function addSubSkillToFocused() {
    if (!actionsTargetNode || !actionsTargetNode.id) return;
    document.location.href = imixsOfficeMain.contextPath + "/pages/admin/skill.jsf?parent=" + actionsTargetNode.id;
}

// Define public namespace
var imixsOfficeSkillBubble = IMIXS.org.imixs.workflow.skillbubble;