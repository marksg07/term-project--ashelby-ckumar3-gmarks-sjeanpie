class Ball{

    /**
     * Constructor, storing orienation and waydata
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
     * This will tell us whether or not this tile is within the original.
     *
     * @returns
     */
    setPosition(x, y){
        this.ctx.fillStyle = "black";
        this.ctx.fillRect(this.x, this.y, this.width, this.height);
        this.ctx.fillStyle = this.fillStyle;
        this.ctx.fillRect(x, y, this.width, this.height);
    }

    setFillStyle(fill) {
        this.fillStyle = fill;
    }
}