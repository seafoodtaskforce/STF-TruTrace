"use strict";
function isLeftButtonClicked(e) {
    return e.button === MouseButtons.Left;
}
exports.isLeftButtonClicked = isLeftButtonClicked;
function isRightButtonClicked(e) {
    return e.button === MouseButtons.Right;
}
exports.isRightButtonClicked = isRightButtonClicked;
function isEscapePressed(e) {
    return e.keyCode === Keys.Escape;
}
exports.isEscapePressed = isEscapePressed;
(function (Keys) {
    Keys[Keys["Escape"] = 27] = "Escape";
})(exports.Keys || (exports.Keys = {}));
var Keys = exports.Keys;
(function (MouseButtons) {
    MouseButtons[MouseButtons["Left"] = 0] = "Left";
    MouseButtons[MouseButtons["Right"] = 2] = "Right";
})(exports.MouseButtons || (exports.MouseButtons = {}));
var MouseButtons = exports.MouseButtons;
//# sourceMappingURL=event.utils.js.map