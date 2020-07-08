XMLHttpRequest.prototype.origOpen = XMLHttpRequest.prototype.open;
XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
// these will be the key to retrieve the payload
    this.recordedMethod = method;
    this.recordedUrl = url;
    this.origOpen(method, url, async, user, password);
    this.origonreadystatechange = this.onreadystatechange;
    this.onreadystatechange=() => {
        if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
            recorder.recordPayload(this.recordedMethod, this.recordedUrl, this.response, false);
        }
        this.origonreadystatechange();
    };
};
XMLHttpRequest.prototype.origSend = XMLHttpRequest.prototype.send;
XMLHttpRequest.prototype.send = function(body) {
    recorder.recordPayload(this.recordedMethod, this.recordedUrl, body, true);
    const method = this.recordedMethod;
    const url = this.recordedUrl;
    return this.origSend(body);
};
