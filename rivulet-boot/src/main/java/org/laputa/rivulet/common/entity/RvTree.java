package org.laputa.rivulet.common.entity;

/**
 * @author JQH
 * @since 下午 8:17 22/07/21
 */
public abstract class RvTree<PkType> extends RvEntity<PkType> {
    /**
     * 获取树形对象的父节点ID
     *
     * @return
     */
    protected abstract PkType getParentId();
}
