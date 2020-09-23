export function angleDistance(a: number, b: number) {
    let d: number = b - a;
    while (d < -Math.PI) {
        d += 2 * Math.PI;
    }
    while (d > Math.PI) {
        d -= 2 * Math.PI;
    }
    return d;
}