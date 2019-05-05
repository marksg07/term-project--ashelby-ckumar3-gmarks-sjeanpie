class Ball {

    /**
     * Constructor for ball.
     * @param size ball width and height.
     * @param ctx context to draw one.
     * @param x initial x.
     * @param y initial y.
     */
    constructor(size, ctx, x, y) {
        this.hidden = false;
        this.x = x;
        this.y = y;
        this.size = size;
        this.ctx = ctx;
        this.fillStyle = "white";
        this.ctx.fillStyle = this.fillStyle;
        this.ctx.fillRect(x - (size / 2), y - (size / 2), size, size);
    }

    /**
     * Sets position.
     * @param x resets x value.
     * @param y resets y value.
     */
    setPosition(x, y) {
        if (!this.hidden) {
            this.ctx.fillStyle = "black";
            this.ctx.fillRect(this.x - (this.size / 2) - 1, this.y - (this.size / 2) - 1, this.size + 2, this.size + 2);
            this.ctx.fillStyle = this.fillStyle;
            this.ctx.fillRect(x - (this.size / 2), y - (this.size / 2), this.size, this.size);
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Draws the paddle.
     */
    draw() {
        this.ctx.fillStyle = this.fillStyle;
        this.ctx.fillRect(x - (this.size / 2), y - (this.size / 2), this.size, this.size);
    }

    /**
     * Hides the paddle.
     */
    hide() {
        this.hidden = true;
        this.ctx.fillStyle = "black";
        this.ctx.fillRect(this.x - (this.size / 2) - 1, this.y - (this.size / 2) - 1, this.size + 2, this.size + 2);
    }

    /**
     * Shows the paddle.
     */
    show() {
        this.hidden = false;
        this.ctx.fillStyle = "white";
    }

    /**
     * Change fill of ball.
     * @param fill
     */
    setFillStyle(fill) {
        this.fillStyle = fill;
    }
}