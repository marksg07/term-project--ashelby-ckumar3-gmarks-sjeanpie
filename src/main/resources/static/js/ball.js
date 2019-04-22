class Ball{

    /**
     *
     * @param size ball width and height.
     * @param ctx context to draw one.
     * @param x initial x.
     * @param y initial y.
     */
    constructor(size, ctx, x, y){
        this.x = x;
        this.y = y;
        this.size = size;
        this.ctx = ctx;
        this.fillStyle = "white";
        this.ctx.fillStyle = this.fillStyle;
        this.ctx.fillRect(x, y, size, size);
    }

    /**
     *
     * @param x resets x value.
     * @param y resets y value.
     */
    setPosition(x, y){
        this.ctx.fillStyle = "black";
        this.ctx.fillRect(this.x - 2, this.y - 2, this.size + 4, this.size + 4);
        this.ctx.fillStyle = this.fillStyle;
        this.ctx.fillRect(x, y, this.size, this.size);
        this.x = x;
        this.y = y;
    }

    /**
     * Change fill of ball
     * @param fill
     */
    setFillStyle(fill) {
        this.fillStyle = fill;
    }
}