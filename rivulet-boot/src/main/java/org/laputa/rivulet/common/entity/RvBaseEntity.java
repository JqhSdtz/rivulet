package org.laputa.rivulet.common.entity;

import org.laputa.rivulet.common.validation.RvValidationGroup;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.data.util.ProxyUtils;

import java.io.Serializable;

/**
 * @author JQH
 * @since 下午 8:42 22/04/04
 */
public abstract class RvBaseEntity<PkType> implements Serializable {
    /**
     * 获取实体对象ID
     *
     * @return 实体对象的ID
     */
    public abstract PkType getId();

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!getClass().equals(ProxyUtils.getUserClass(obj))) {
            return false;
        }
        AbstractPersistable<?> that = (AbstractPersistable<?>) obj;
        return null != this.getId() && this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public interface Persist extends RvValidationGroup {
    }

    public interface Update extends RvValidationGroup {
    }
}
