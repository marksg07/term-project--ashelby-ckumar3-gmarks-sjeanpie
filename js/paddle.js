class Paddle{

    /**
     * Constructor, storing orienation and waydata
     */
    constructor(x, y, width, height, ctx) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.ctx = ctx;
        this.fillStyle = "white";
        this.ctx.fillStyle = this.fillStyle;
        this.ctx.fillRect(x, y, width, height);
    }

    /**
     * This will tell us whether or not this tile is within the original.
     *
     * @returns
     */
    setPosition(y){
        this.ctx.fillStyle = "black";
        this.ctx.fillRect(this.x, this.y, this.width, this.height);
        this.ctx.fillStyle = this.fillStyle;
        this.ctx.fillRect(this.x, y, this.width, this.height);
        this.y = y;
    }

    setFillStyle(fill) {
        this.fillStyle = fill;
    }
}