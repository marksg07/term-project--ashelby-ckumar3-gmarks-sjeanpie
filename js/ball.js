class Ball{

    /**
     * Constructor, storing orienation and waydata
     */
    constructor(size, ctx){
        this.size = size;
        this.ctx = ctx;
        this.ctx.fillStyle = "white";
    }

    /**
     * This will tell us whether or not this tile is within the original.
     *
     * @returns
     */
    setPosition(x, y){
        this.ctx.fillRect(x, y, size, size);
    }
}