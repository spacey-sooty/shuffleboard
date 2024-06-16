package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.tab.model.ComponentModel;
import edu.wpi.first.shuffleboard.api.tab.model.WidgetModel;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * A Layout is a dashboard Component that holds other Components (i.e., widgets or other layouts) in a nested fashion.
 */
public interface Layout extends Component, ComponentContainer {

  /**
   * Gets all the children in this layout.
   */
  Collection<Component> getChildren();

  /**
   * Adds a new component to this layout.
   *
   * @param component the component to add
   */
  void addChild(Component component);

  /**
   * Adds a new component to this layout from a drag-and-drop operation. By default, this will ignore the drop point
   * and simply call {@link #addChild(Component)}. Whether or not the drop coordinates are used is up to the specific
   * layout implementation.
   *
   * @param component the component to add
   * @param x         the x-coordinate, relative to this layout, that the component was dropped at
   * @param y         the y-coordinate, relative to this layout, that the component was dropped at
   */
  default void addChild(Component component, double x, double y) {
    addChild(component);
  }

  /**
   * Removes a child component from ths layout.
   *
   * @param component the component to remove
   */
  void removeChild(Component component);

  @Override
  default void removeComponent(Component component) {
    removeChild(component);
  }

  @Override
  default void addComponent(Component component) {
    addChild(component);
  }

  @Override
  @SuppressWarnings("PMD.UseExplicitTypes")
  default Component addComponent(ComponentModel model) {
    var optionalComponent = Components.getDefault().createComponent(model.getDisplayType());
    if (model instanceof WidgetModel) {
      optionalComponent
          .ifPresent((c -> {
            ((Widget) c).addSource(((WidgetModel) model).getDataSource());
            addComponent(c);
          }));
    } else {
      optionalComponent.ifPresent(this::addComponent);
    }
    return optionalComponent.orElse(null);
  }

  @Override
  default Stream<Component> components() {
    return getChildren().stream();
  }

  @Override
  default Stream<Component> allComponents() {
    return Stream.concat(Stream.of(this), getChildren().stream().flatMap(Component::allComponents));
  }
}
