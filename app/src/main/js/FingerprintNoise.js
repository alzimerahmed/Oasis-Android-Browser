// Adds deterministic-per-session noise to canvas and WebGL readbacks to reduce
// fingerprinting surface. Applied only when fingerprint randomization is enabled.
(function () {
  if (window.__oasisFingerprintNoise) return;
  window.__oasisFingerprintNoise = true;
  var seed = Math.floor(Math.random() * 8) + 1;
  function noise(data) {
    for (var i = 0; i < data.length; i += 4) {
      data[i] = Math.min(255, data[i] + ((i * seed) % 3) - 1);
    }
  }
  var toDataURL = HTMLCanvasElement.prototype.toDataURL;
  HTMLCanvasElement.prototype.toDataURL = function () {
    try {
      var ctx = this.getContext('2d');
      if (ctx) {
        var image = ctx.getImageData(0, 0, Math.min(this.width, 16), Math.min(this.height, 16));
        noise(image.data);
      }
    } catch (e) { /* cross-origin canvas */ }
    return toDataURL.apply(this, arguments);
  };
  var getImageData = CanvasRenderingContext2D.prototype.getImageData;
  HTMLCanvasElement.prototype.getContext = new Proxy(HTMLCanvasElement.prototype.getContext, {
    apply: function (target, thisArg, args) {
      var context = Reflect.apply(target, this, arguments);
      if (context && context.getImageData) {
        var getImageData = context.getImageData;
        context.getImageData = function (sx, sy, sw, sh) {
          var image = getImageData.call(this, Math.max(0, sx), Math.max(0, sy), Math.min(sw, 16), Math.min(sh, 16));
          noise(image.data);
          return image;
        };
      }
      return context;
    }
  });
})();
