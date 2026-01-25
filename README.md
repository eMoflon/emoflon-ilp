# eMoflon-ILP

The goal of this plug-in is to simplify the use of different ILP solvers in Eclipse projects.
Therefore, the plug-in provides an interface to specify the ILP problem only once instead of having to do different implementations for each solver individually.
Switching between solvers is done by changing one parameter in the solver configuration.


## How to install the plug-in

* Install at least one of the supported ILP solvers:
    * Install [Gurobi](https://www.gurobi.com/) in version `13.0.1` and activate a license for your computer.
    * Install [GLPK](https://www.gnu.org/software/glpk/) (free and open-source) in the newest version (`4.65`) and add it to your path.
      * For Windows-based systems, follow these steps to install GLPK:
        * Download [winglpk](https://sourceforge.net/projects/winglpk/files/winglpk/GLPK-4.65/).
        * Extract the archive, e.g., to `C:\Program Files\GLPK\glpk-4.65`.
        * Add `C:\Program Files\GLPK\glpk-4.65\w64` to the system-wide environment variable `path`.
        * Restart your Eclipse IDE.
    * Install [CPLEX](https://www.ibm.com/analytics/cplex-optimizer) in version `22.1.2` and activate a license for your computer (if necessary).
 
- Build + install the project to the local `.m2/` folder:  
  `$ mvn clean install`


## How to build the plugin

* Install at least one of the supported ILP solvers:
    * Install [Gurobi](https://www.gurobi.com/) in version `13.0.1` and activate a license for your computer.
    * Install [GLPK](https://www.gnu.org/software/glpk/) (free and open-source) in the newest version (`4.65`) and add it to your path.
      * For Windows-based systems, follow these steps to install GLPK:
        * Download [winglpk](https://sourceforge.net/projects/winglpk/files/winglpk/GLPK-4.65/).
        * Extract the archive, e.g., to `C:\Program Files\GLPK\glpk-4.65`.
        * Add `C:\Program Files\GLPK\glpk-4.65\w64` to the system-wide environment variable `path`.
        * Restart your Eclipse IDE.
    * Install [CPLEX](https://www.ibm.com/analytics/cplex-optimizer) in version `22.1.2` and activate a license for your computer (if necessary).
 
- Build the project + feature + update site:  
  `$ mvn clean package`


## How to run tests

Remember: Depending on the solver a license is necessary (e.g., for Gurobi).

- Run all tests:  
  `$ mvn clean verify`
- Run a specific test class (e.g., [GlpkTest.java](org.emoflon.ilp.tests/src/org/emoflon/ilp/tests/GlpkTest.java)):  
  `$ mvn -Dtest=GlpkTest -DfailIfNoTests=false verify`

Before running tests with the CPLEX solver, it might be necessary to add the following Run Configuration to the VM Arguments (Eclipse: right click on the project -> `Run as` -> `Run Configurations` -> `Arguments` tab), replace with the appropriate path, for example:  
`-Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio2212/cplex/bin/x86-64_linux`


## How to use the plugin

When using the plugin within the Eclipse IDE, the following environment variables may be necessary for your runtime configuration:
```
# Linux/macOS
GRB_LICENSE_FILE=/home/mkratz/gurobi.lic
GUROBI_HOME=/opt/gurobi1301/linux64/
LD_LIBRARY_PATH=/opt/gurobi1301/linux64/lib/:/opt/ibm/ILOG/CPLEX_Studio2212/cplex/bin/x86-64_linux/
PATH=/opt/gurobi1301/linux64/bin/:/opt/ibm/ILOG/CPLEX_Studio2212/cplex/bin/x86-64_linux/:$PATH

# Windows
GRB_LICENSE_FILE=C:\Users\mkratz\gurobi.lic
GUROBI_HOME=C:\gurobi1301\win64
LD_LIBRARY_PATH=C:\gurobi1301\win64\lib;C:\Program Files\IBM\ILOG\CPLEX_Studio2212\cplex\bin\x64_win64\
PATH=C:\gurobi1301\win64\bin;C:\Program Files\IBM\ILOG\CPLEX_Studio2212\cplex\bin\x64_win64\
```


## Example Problem

### Knapsack Problem

This example can be found in the test class [SolverTest.java](org.emoflon.ilp.tests/src/org/emoflon/ilp/tests/SolverTest.java).

There is a set of items, which all have a weight and a value.
The goal is to determine a collection of items, for which the profit is maximized but the capacity constraint is satisfied.

```
// Amount of items
int I = 6;
// Profit
int[] p = { 10, 13, 18, 32, 7, 15 };
// Weight
int[] w = { 11, 15, 20, 35, 10, 33 };
// Capacity
int c = 47;

// Create variables:
// 0 -> item i not put in knapsack
// 1 -> item i put in knapsack
List<BinaryVariable> x_i = new ArrayList<>();
for (int i = 0; i < I; i++) {
	x_i.add(new BinaryVariable("x_" + i));
}

// Objective: maximize the total price of selected items
// maximize SUM(p_i * x_i)
Problem problem = new Problem();
problem.setType(ObjectiveType.MAX);

LinearFunction lin = new LinearFunction();
for (int i = 0; i < I; i++) {
	lin.addTerm(x_i.get(i), p[i]);
}

// Constraint: Total weight must be equal or less than the capacity
// SUM(w_i * x_i) <= c
LinearConstraint c1 = new LinearConstraint(Operator.LESS_OR_EQUAL, c);
for (int i = 0; i < I; i++) {
	c1.addTerm(x_i.get(i), w[i]);
}

// Model
problem.setObjective(lin);
problem.add(c1);

// Optimize
SolverConfig config = new SolverConfig(SolverType.GLPK, false, 0.0, true, 42, false, 0.0, false, 0, 0, true, false, false, null);
Solver solver = (new SolverHelper(config)).getSolver();
solver.buildILPProblem(problem);
SolverOutput out = solver.solve();
// Prints the result of the objective
System.out.println(out.toString());
// Sets the values for the Variables
solver.updateValuesFromSolution();

// Do something, e.g. print

solver.terminate();
```

## Repository/Project structure

| **Name**                        | **Description**                                                        |
| ------------------------------- | ---------------------------------------------------------------------- |
| `org.emoflon.ilp`               | Contains the implementation of the plug-in.                            |
| `org.emoflon.ilp.feature`       | Contains the information for the feature to export.                    |
| `org.emoflon.ilp.updatesite`    | Contains the update site configuration (to include the feature above). |
| `org.emoflon.ilp.dependencies`  | Contains all necessary dependencies (JARs of each solver).             |
| `org.emoflon.ilp.tests`         | Contains all tests and test-related content.                           |
| `ci.yml`                        | GitHub Actions configuration to build and test the plug-in.            |
| `pom.xml`                       | Maven configuration file that contains the parent group/project.       |


## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for more details.
