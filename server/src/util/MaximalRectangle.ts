export function maximalRectangle(matrix) {
    var m;
    var n;
    var M = matrix[0].length;
    var N = matrix.length;
    var c = [];
    var s = [];
    var best_ll = { col: 0, row: 0 };
    var best_ur = { col: -1, row: -1 };
    var best_area = 0;

    for (m = 0; m < M + 1; m++) {
        c[m] = 0;
        s[m] = { col: 0, row: 0 };
    }
    for (n = 0; n < N; n++) {
        for (m = 0; m < M; m++) {
            c[m] = matrix[n][m] == 1 ? (c[m] + 1) : 0;
        }
        var open_width = 0;
        for (m = 0; m < M + 1; m++) {
            if (c[m] > open_width) {
                s.push({ col: m, row: open_width });
                open_width = c[m];
            } else if (c[m] < open_width) {
                var m0;
                var n0;
                var area;
                do {
                    var cell = s.pop();
                    m0 = cell.col;
                    n0 = cell.row;
                    area = open_width * (m - m0);
                    if (area > best_area) {
                        best_area = area;
                        best_ll = { col: m0, row: n };
                        best_ur = { col: m - 1, row: n - open_width + 1 };
                    }
                    open_width = n0;
                } while (c[m] < open_width);
                open_width = c[m];
                if (open_width != 0) {
                    s.push({ col: m0, row: n0 });
                }
            }
        }
    }
    return {
        x1: best_ur.row,
        y1: best_ll.col,
        x2: best_ll.row,
        y2: best_ur.col,
    }
}