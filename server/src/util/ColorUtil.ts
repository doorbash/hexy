const stroke_colors = [
    "#2196f3ff", // blue
    "#795548ff", // brown
    "#4caf50ff", // green
    "#ff9800ff", // orange
    "#9c27b0ff", // purple
    "#f44336ff", // red
    "#3f51b5ff", // indigo

    "#c4c340ff",
    "#9e2e2eff",
    "#be364aff",
    "#00a5a3ff",
    "#a51f8aff",
    "#5b9220ff",
    "#a64f08ff",
    "#ba9633ff",
    "#a81640ff",
    "#008bb8ff",
    "#d128dbff",
    "#14954eff",
    "#b4c74cff",
    "#2ea67dff",
    "#4db23aff"
];

const fill_colors = [
    "#6ec6ffff",
    "#a98274ff",
    "#80e27eff",
    "#ffc947ff",
    "#d05ce3ff",
    "#ff7961ff",
    "#757de8ff",

    "#fffd46ff",
    "#fd3535ff",
    "#ff5e75ff",
    "#2cfefcff",
    "#fe2cd4ff",
    "#94ff22ff",
    "#ff9844ff",
    "#ffc832ff",
    "#ff1c5dff",
    "#13c5ffff",
    "#f662ffff",
    "#22ff85ff",
    "#edff89ff",
    "#89ffd7ff",
    "#9cff89ff"
];

export function getRandomFillStrokePair() {
    let randomIndex = Math.floor(Math.random() * stroke_colors.length);
    return {
        fill: fill_colors[randomIndex],
        stroke: stroke_colors[randomIndex]
    }
}

export function getFillStrokePair(index: number) {
    return {
        fill: fill_colors[(index - 1) % stroke_colors.length],
        stroke: stroke_colors[(index - 1) % stroke_colors.length]
    }
}