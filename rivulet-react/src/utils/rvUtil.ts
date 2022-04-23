export default {
    equalAndNotEmpty(obj1, obj2) {
        if (obj1 === null || typeof obj1 === 'undefined') {
            return false;
        } else if (obj2 === null || typeof obj2 === 'undefined') {
            return false;
        } else {
            return obj1 === obj2;
        }
    }
}
