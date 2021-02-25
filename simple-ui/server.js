let http = require('http');
let nodeStatic = require('node-static');
let fileServer = new nodeStatic.Server('.');
function up(req, res) {
    res.write('ok')
    res.end()
}
function accept(req, res) {
    if (req.url === '/up') {
        up(req, res);
        return;
    }
    fileServer.serve(req, res);
}
if (!module.parent) {
    console.log('demo started')
    http.createServer(accept).listen(3000);
} else {
    exports.accept = accept;
}