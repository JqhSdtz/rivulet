function fillFlattedItemList(flattedItemList: any[], itemList: any[], linkField: string) {
    itemList.forEach(item => {
       flattedItemList.push(item);
       if (item[linkField] instanceof Array) {
           fillFlattedItemList(flattedItemList, itemList, linkField);
       }
    });
}

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
        if (!target || !obj) return;
        for (const [key, value] of Object.entries(obj)) {
            if (preserveTarget && typeof target[key] !== 'undefined') {
                continue;
            }
            target[key] = value;
        }
    },
    flatItems(itemList: any[], linkField: string) {
        const flattedItemList = [];
        fillFlattedItemList(flattedItemList, itemList, linkField);
        return flattedItemList;
    }
};
