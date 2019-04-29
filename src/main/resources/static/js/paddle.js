class Paddle{

    /**
     *
     * @param x initial x value of the paddle.
     * @param y initial y value of the paddle.
     * @param width width of the paddle.
     * @param height height of the paddle.
     * @param ctx context to be drawing on.
     */
    constructor(x, y, width, height, ctx) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.ctx = ctx;
        this.fillStyle = "white";
        this.ctx.fillStyle = this.fillStyle;
        this.ctx.fillRect(x-(width/2), y-(height/2), width, height);
    }


    /**
     *
     * @param y only y position will change.
     */
    setPosition(y){
        this.ctx.fillStyle = "black";
        this.ctx.fillRect(this.x -(this.width/2) - 1, this.y - (this.height/2) - 1, this.width + 2, this.height + 2);
        this.ctx.fillStyle = this.fillStyle;
        this.ctx.fillRect(this.x-(this.width/2), y-(this.height/2), this.width, this.height);
        this.y = y;
    }

    /**
     * Change fill of paddle
     * @param fill
     */
    setFillStyle(fill) {
        this.fillStyle = fill;
    }
}