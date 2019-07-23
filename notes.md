# General notes for contributors

This file documents all changes from the previous GreenUML version. It describes added features and explains how they function in a bit more detail. Also, some notes about previously existing code can be found here.


### New AutoLayout option

A new layout option has been added. This layout is suited for tree-like structures (for example, showing child classes of a superclass). For more complex diagrams, the old AutoLayout option is preferred, but this new option can make simpler diagrams more comprehensible.

This method works by finding all classes that are not subclasses of another class present in the diagram. For each of these classes, another method is called which organizes its model (and its child models) into a single tree like structure. The trees are created individually so that child classes can be as close as possible to their parent class.

Models are sorted on the y axis by level (depth). Two more methods have been added for this purpose- one to find the maximum level across all models in the diagram, and one to find the relative level of an individual model. The difference between the maximum and individual level is the depth at which the model is placed at, with level 0 being the top.

In the future, this method could be improved by making the parent classes centered relative to their subclasses.

### Dark mode

A rather simple addition which darkens the color scheme. This can be activated from the Preferences tab.

## Notes about the project in general

To make it easier for people to start improving the project, here is a general description of some of the previously existing structure and features of the code.

#### Graph

In order to make layouts possible, a graph structure is used. It represents the different types of relationships between the classes. By accessing incoming and outgoing edges, all relationships for a single model can be found.
CCVisu methods are used in the default layout method in order to arrange the graph.

#### Changing the layout

Instead of adding the models to some kind of panel, all that needs to be done in order to update the layout is to change the properties of the models (for example, the position) and check for changes in the editor.

#### Bugs

 - Default layout method causes stack overflow error when used with a large number of models

