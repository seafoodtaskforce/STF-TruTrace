package com.wwf.shrimp.application.client.android.models.dto;

/**
 * Base class for all entities that have an id
 * @author AleaActaEst
 *
 */
public class IdentifiableEntity {

    /**
     * The unique id for this entity
     */
    private long id;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "IdentifiableEntity{" +
                "id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentifiableEntity)) return false;

        IdentifiableEntity that = (IdentifiableEntity) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
