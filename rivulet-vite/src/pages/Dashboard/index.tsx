import {useContext, useState} from 'react';
import {TabNodeContext, TabNodeContextType} from '@/layouts/BasicLayout';
import {useActivate} from 'react-activation';
import {closestCenter, DndContext, PointerSensor, useSensor, useSensors} from '@dnd-kit/core';
import {arrayMove, SortableContext, useSortable, verticalListSortingStrategy} from '@dnd-kit/sortable';
import {CSS} from '@dnd-kit/utilities';

const SortableItem = (props) => {
    const {
        attributes,
        listeners,
        setNodeRef,
        transform,
        transition
    } = useSortable({id: props.id});

    const style = {
        transform: CSS.Transform.toString(transform),
        transition
    };
    return (
        <div ref={setNodeRef}
             {...attributes}
             {...listeners}
             style={{...style, width: 50, height: 50, margin: 20, backgroundColor: '#eee', textAlign: 'center'}}
        >
            {props.id}
        </div>
    );
};

const SortableContainer = () => {
    const [items, setItems] = useState(['1', '2', '3']);
    const sensors = useSensors(
        useSensor(PointerSensor)
    );

    function handleDragEnd(event) {
        const {active, over} = event;

        if (active.id !== over.id) {
            setItems((items) => {
                const oldIndex = items.indexOf(active.id);
                const newIndex = items.indexOf(over.id);

                return arrayMove(items, oldIndex, newIndex);
            });
        }
    }

    return (
        <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragEnd={handleDragEnd}
        >
            <SortableContext
                items={items}
                strategy={verticalListSortingStrategy}
            >
                {items.map(id => <SortableItem key={id} id={id}/>)}
            </SortableContext>
        </DndContext>
    );
};

export default () => {
    const {
        closeTab,
        beforeClose
    } = useContext<TabNodeContextType>(TabNodeContext);
    beforeClose((clearAttention, doClose) => {
        setTimeout(clearAttention, 300);
        setTimeout(doClose, 400);
        return false;
    });
    const [counter, setCounter] = useState(0);
    const [activeCounter, setActiveCounter] = useState(0);
    useActivate(() => {
        setActiveCounter(activeCounter + 1);
    });
    return (
        <div style={{margin: '20px'}}>
            <div>
                <h2>Dashboard page {counter}</h2>
                <h3>Dashboard page has been activated {activeCounter} times</h3>
            </div>
            <button onClick={() => setCounter(counter + 1)}>Add</button>
            <button onClick={() => closeTab()} style={{marginLeft: '3rem'}}>Close Tab</button>
            <SortableContainer/>
        </div>
    );
};
