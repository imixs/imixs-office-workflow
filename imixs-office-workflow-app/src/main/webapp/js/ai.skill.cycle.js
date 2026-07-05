"use strict";

// define script name spaces
IMIXS.namespace("org.imixs.workflow.skillcycle");

// tree data - provided by SkillController.getSkillTreeJson() via skill-cycle.xhtml
var data = (typeof imixsSkillTreeData !== "undefined") ? imixsSkillTreeData : [];

// layout configuration
const BOX_W = 240, BOX_H = 100, GAP = 20;
const MAX_DEPTH_COLOR = 4;
const CENTER_W = 330, CENTER_H = 132;
const CENTER_GAP_H = 135, CENTER_GAP_V = 110;

// internal state, populated on DOMContentLoaded
var root;
var current;
var stage;
var crumbEl;
var linesG;
const NS = 'http://www.w3.org/2000/svg';

/**
 * Init method for the skill cycle page.
 * Builds the tree structure and renders the initial screen (root level).
 */
document.addEventListener("DOMContentLoaded", function () {
    root = { name: 'Skills', children: data };
    prepare(root, null, 0);

    stage = document.getElementById('radial-stage');
    crumbEl = document.getElementById('radial-crumb');
    linesG = document.getElementById('radial-lines');

    renderScreen(root);
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
 * Computes horizontal and vertical ring radii separately, so the ring
 * reads as an ellipse: wide enough to clear the center box left/right,
 * but noticeably tighter above/below it.
 */
function ringRadii(n) {
    var rx = CENTER_W / 2 + CENTER_GAP_H + BOX_W / 2;
    var ry = CENTER_H / 2 + CENTER_GAP_V + BOX_H / 2;

    if (n > 1) {
        var neededCirc = n * (BOX_W + GAP);
        // Ramanujan's approximation for an ellipse's circumference
        var currentCirc = Math.PI * (3 * (rx + ry) - Math.sqrt((3 * rx + ry) * (rx + 3 * ry)));
        if (currentCirc < neededCirc) {
            var factor = neededCirc / currentCirc;
            rx *= factor;
            ry *= factor;
        }
    }
    return { rx: rx, ry: ry };
}


/**
 * Rebuilds the breadcrumb as a row of clickable boxes (title + description),
 * one per ancestor from the root down to the currently focused node.
 * Clicking any box except the last (active) one navigates directly there.
 */
function updateCrumb() {
    var chain = [];
    var n = current;
    while (n) {
        chain.unshift(n);
        n = n.parent;
    }

    crumbEl.innerHTML = '';
    chain.forEach(function (node, i) {
        if (i > 0) {
            var sep = document.createElement('span');
            sep.className = 'crumb-sep';
            sep.textContent = '›';
            crumbEl.appendChild(sep);
        }

        var isActive = (i === chain.length - 1);
        var box = document.createElement('span');
        box.className = 'crumb-box' + (isActive ? ' active' : '');
       
        var title = document.createElement('span');
        title.className = 'radial-title';
        title.textContent = node.name;
        box.appendChild(title);

        var desc = document.createElement('span');
        desc.className = 'radial-desc';
        if (node === root) {
            desc.textContent = 'Skill Übersicht';
        } else {
            desc.textContent = node.description || '';
        }
        box.appendChild(desc);

        if (!isActive) {
            box.addEventListener('click', function () {
                animateTo(node);
            });
        }
        crumbEl.appendChild(box);

    });
}

/**
 * Renders the current screen: the focused node in the center, its
 * direct children arranged in a circle around it, connected by lines.
 */
function renderScreen(focus) {
    current = focus;
    updateCrumb();

    var children = focus.children || [];
    var n = children.length;
    var ring = ringRadii(n);
    var halfY = ring.ry + BOX_H / 2 + 30;

    var width = Math.max(stage.clientWidth || 0, ring.rx * 2 + BOX_W + 60);
    var height = Math.max(360, halfY * 2);
    stage.style.height = height + 'px';

    var cx = width / 2, cy = height / 2;

    var svg = stage.querySelector('.radial-svg');
    svg.setAttribute('viewBox', '0 0 ' + width + ' ' + height);
    linesG.innerHTML = '';

    stage.querySelectorAll('.radial-box').forEach(function (el) {
        el.remove();
    });

    renderCenterBox(focus, cx, cy);

    children.forEach(function (child, i) {
        var angle = (i / n) * 2 * Math.PI - Math.PI / 2;
        var x = cx + ring.rx * Math.cos(angle);
        var y = cy + ring.ry * Math.sin(angle);
        renderLine(cx, cy, x, y);
        renderChildBox(child, x, y);
    });
}

/**
 * Renders the (larger) center box for the currently focused node.
 * Shows title + description, or a hint text when focused on the
 * synthetic root node (which has no id or description of its own).
 */
function renderCenterBox(focus, cx, cy) {
    var box = document.createElement('div');
    box.className = 'radial-box depth-0 radial-center-box';
    box.style.left = cx + 'px';
    box.style.top = cy + 'px';

    var topic = document.createElement('span');
    topic.className = 'radial-topic';
    topic.textContent = focus.topic || focus.name;
    box.appendChild(topic);

    var desc = document.createElement('span');
    desc.className = 'radial-desc';
    if (focus === root) {
        desc.textContent = 'Klicken Sie auf die Skill Knoten um diese zu bearbeiten';
    } else {
        desc.textContent = focus.description || '';
    }
    box.appendChild(desc);

    if (focus !== root) {
        var idLabel = document.createElement('span');
        idLabel.className = 'radial-id';
        idLabel.textContent = focus.name;
        box.appendChild(idLabel);
    }

    if (focus.id) {
        box.appendChild(makeActions(focus.id));
    }
    if (focus.parent) {
        box.addEventListener('click', function () {
            animateTo(focus.parent);
        });
    } else {
        box.style.cursor = 'default';
    }
    stage.appendChild(box);
    requestAnimationFrame(function () {
        box.classList.add('shown');
    });
}


/**
 * Renders a single clickable child box, including title, description
 * and edit / add-sub-skill actions.
 */
function renderChildBox(child, x, y) {
    var depthClass = 'depth-' + Math.min(child.depth, MAX_DEPTH_COLOR);
    var box = document.createElement('div');
    box.className = 'radial-box radial-child-box ' + depthClass;
    box.style.left = x + 'px';
    box.style.top = y + 'px';
    box.title = child.topic;

    var topic = document.createElement('span');
    topic.className = 'radial-topic';
    topic.textContent = child.topic;
    box.appendChild(topic);

    var desc = document.createElement('span');
    desc.className = 'radial-desc';
    desc.textContent = child.description || '';
    box.appendChild(desc);

    var idLabel = document.createElement('span');
    idLabel.className = 'radial-id';
    idLabel.textContent = 'Id: '+child.name;
    box.appendChild(idLabel);

    if (child.id) {
        box.appendChild(makeActions(child.id));
    }
    box.addEventListener('click', function () {
        animateTo(child);
    });
    stage.appendChild(box);
    requestAnimationFrame(function () {
        box.classList.add('shown');
    });
}


/**
 * Creates the hover-revealed edit / add-sub-skill action row for a box.
 */
function makeActions(id) {
    var actions = document.createElement('span');
    actions.className = 'radial-box-actions';

    var editLink = document.createElement('a');
    editLink.href = '#';
    editLink.title = 'Edit';
    editLink.innerHTML = '<i class="fa-solid fa-pen"></i>';
    editLink.addEventListener('click', function (e) {
        e.stopPropagation();
        e.preventDefault();
        editSkill(id);
    });

    var addLink = document.createElement('a');
    addLink.href = '#';
    addLink.title = 'Add sub-skill';
    addLink.innerHTML = '<i class="fa-solid fa-diagram-project"></i>';
    addLink.addEventListener('click', function (e) {
        e.stopPropagation();
        e.preventDefault();
        addSubSkill(id);
    });

    actions.appendChild(editLink);
    actions.appendChild(addLink);
    return actions;
}

/**
 * Draws a connector line between the center and a child box.
 */
function renderLine(x1, y1, x2, y2) {
    var line = document.createElementNS(NS, 'line');
    line.setAttribute('class', 'radial-line');
    line.setAttribute('x1', x1); line.setAttribute('y1', y1);
    line.setAttribute('x2', x2); line.setAttribute('y2', y2);
    linesG.appendChild(line);
    requestAnimationFrame(function () {
        line.classList.add('shown');
    });
}

/**
 * Fades out the current screen, then renders the new focus node.
 */
function animateTo(focus) {
    var boxes = stage.querySelectorAll('.radial-box');
    var lines = stage.querySelectorAll('.radial-line');
    boxes.forEach(function (b) { b.classList.remove('shown'); });
    lines.forEach(function (l) { l.classList.remove('shown'); });
    setTimeout(function () {
        renderScreen(focus);
    }, 180);
}

/**
 * Navigates to the edit page of the given skill.
 */
function editSkill(id) {
    document.location.href = imixsOfficeMain.contextPath + "/pages/admin/skill.jsf?id=" + id;
}

/**
 * Navigates to the create page for a new sub-skill under the given skill.
 */
function addSubSkill(id) {
    document.location.href = imixsOfficeMain.contextPath + "/pages/admin/skill.jsf?parent=" + id;
}

// Define public namespace
var imixsOfficeSkillCycle = IMIXS.org.imixs.workflow.skillcycle;