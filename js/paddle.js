class Paddle{

    /**
     * Constructor, storing orienation and waydata
     */
    constructor(width, height, ctx) {
        this.width = width;
        this.height = height;
        this.ctx = ctx;
        this.ctx.fillStyle = "white";
    }

    /**
     * This will tell us whether or not this tile is within the original.
     *
     * @returns
     */
    setPosition(x, y){
        this.ctx.fillRect(x, y, this.width, this.height);
    }
}