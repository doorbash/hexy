const TEXTURE_NUMBER_LENGTH = 5;
const LAST_TEXTURE_INDEX = 470;

export function isTextureValid(textureName: string): boolean {
    if(!textureName.startsWith("e")) return false;
    if (textureName.length != TEXTURE_NUMBER_LENGTH + 1) return false;
    let index = parseInt(textureName.substr(2));
    if (index == NaN) return false;
    if (index < 0 || index > LAST_TEXTURE_INDEX) return false;
    return true;
}

export function getRandomTexture(): string {
    let index = Math.floor(Math.random() * 100);
    return "e" + ('' + index).padStart(TEXTURE_NUMBER_LENGTH, "0");
}