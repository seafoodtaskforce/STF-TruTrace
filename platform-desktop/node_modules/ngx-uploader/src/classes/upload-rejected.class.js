var UploadRejected = (function () {
    function UploadRejected() {
    }
    Object.defineProperty(UploadRejected, "EXTENSION_NOT_ALLOWED", {
        get: function () { return 'ExtensionNotAllowed'; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(UploadRejected, "MAX_SIZE_EXCEEDED", {
        get: function () { return 'MaxSizeExceeded'; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(UploadRejected, "MAX_UPLOADS_EXCEEDED", {
        get: function () { return 'MaxUploadsExceeded'; },
        enumerable: true,
        configurable: true
    });
    return UploadRejected;
}());
export { UploadRejected };
//# sourceMappingURL=upload-rejected.class.js.map