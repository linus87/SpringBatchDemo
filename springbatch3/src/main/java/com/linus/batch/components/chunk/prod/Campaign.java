package com.linus.batch.components.chunk.prod;

import java.util.Objects;

public class Campaign {
  private String id;
  public Campaign(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Campaign campaign = (Campaign) o;
    return Objects.equals(id, campaign.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "Campaign{" +
            "id='" + id + '\'' +
            '}';
  }
}
