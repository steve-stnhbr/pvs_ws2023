# RiskItForTheBiscuit
This is an agent for Risk game.

## How to run
1. Clone the repository
2. Run the following command in the root directory of the project: 
```
gradle playAlphaBetaMatch
```
This wil start a match between the programmed agent and the AlphaBetaAgent provided by the course.

## Code
### MCTSTree
This class is responsible for the Monte Carlo Tree Search algorithm. It contains the root node of the tree and the methods to perform the search.
The main code is in the method ```simulate```. This executes the four steps of the algorithm:
1. Selection (MCTSSelectionStrategy)
2. Expansion (MCTSExpansionStrategy)
3. Simulation (MCTSSimulationStrategy)
4. Backpropagation

For each of the stages except for Backpropagation there are classes that represent a strategy for that stage. The class names are listed above in parentheses.
These are the heart of the algorithm and allow easy tweaking of the algorithm. 

#### Selection
Selection uses the MCTSSelectionStrategy class. This class contains the method ```select``` which is called once in the Selection-Phase.
The method returns the node that should be expanded next.

For finding a node that maximizes a certain value, the class ```MaximizationSelectionStrategyTemplate``` can be used. This class uses the ```calculateScore``` method to determine the value of a node.
The node that maximizes this value is returned.

#### Expansion
If a selected node has not been visited before, it needs to be expanded. This is done by the MCTSExpansionStrategy class. This class contains the method ```expand``` which is called once in the Expansion-Phase.
It is expected to create one or multiple node(s) that are the children of the given node. One of these nodes is then to be selected for simulation.
The ```RiskAddAllExpansionStrategyTemplate``` class can be used to create an expansion strategy that creates all possible child nodes. The node that should be simulated is afterwards selected using the ```select``` method in the class
that is to be overwritten.

#### Simulation
In the Simulation phase the MCTSSimulationStrategy class is used. This class contains the method ```simulate``` which is called once in the Simulation-Phase.
The MCTS expects that during the simulation a score is calculated for the given node. This score is then used in the Backpropagation-Phase.

#### Backpropagation
The backpropagation is done by the MCTSTree class. It contains the method ```backpropagate``` which is called once in the Backpropagation-Phase.
The method takes the score that was calculated in the Simulation-Phase and updates the score of all nodes in the path from the given node to the root node.


### getBestAction()
The method is responsible to find the best action to perform by searching the children of the root node for the highest utility value.
