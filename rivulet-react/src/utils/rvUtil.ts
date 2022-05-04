export default {
    equalAndNotEmpty(obj1, obj2) {
        if (obj1 === null || typeof obj1 === 'undefined') {
            return false;
        } else if (obj2 === null || typeof obj2 === 'undefined') {
            return false;
        } else {
            return obj1 === obj2;
        }
    },
    mergeObject(target, obj, preserveTarget: boolean = false) {
        for (const [key, value] of Object.entries(obj)) {
            if (preserveTarget && typeof target[key] !== 'undefined') {
                continue;
            }
            target[key] = value;
        }
    }
}
