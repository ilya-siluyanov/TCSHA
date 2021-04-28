import java.io.FileInputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.*;

public class Main {
    Scanner scanner;
    ArrayList<String> states;
    ArrayList<String> alphabet;
    ArrayList<String> initialStates;
    ArrayList<String> acceptingStates;
    ArrayList<String> transitions;
    AdjacencyMatrixGraph<String, String> graph;

    public static void main(String[] args) {
        try {
            new Main().go();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //TODO: add constraints
    private void go() throws Exception {
        System.setIn(new FileInputStream("input.txt"));
        System.setOut(new PrintStream("output.txt"));
        this.scanner = new Scanner(System.in);
        this.states = checkLine("states");
        this.alphabet = checkLine("alpha");
        this.initialStates = checkLine("initial");
        this.acceptingStates = checkLine("accepting");
        this.transitions = checkLine("trans");

        this.graph = new AdjacencyMatrixGraph<>();
        this.states.forEach(graph::addVertex);
        this.transitions.forEach(x -> {
            String[] splitted = x.split(">");
            graph.addEdge(graph.findVertex(splitted[0]), graph.findVertex(splitted[2]), splitted[1]);
        });
    }

    private String KleeneAlgorithm(int vertices) {
        HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>> R = new HashMap<>();
    }

    private void checkTransitions(ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> transitions) throws Exception {
        for (String transition : transitions) {
            String[] splitted = transition.split(">");
            if (splitted.length != 3) {
                throw new Exception("E0");
            }
        }
    }

    private void checkBelongingToStates(ArrayList<String> states, ArrayList<String> stateList) throws Exception {
        for (String state : states) {
            if (!states.contains(state)) {
                throw new Exception("E1");
            }
        }
    }

    private ArrayList<String> checkLine(String name) throws Exception {
        if (!scanner.hasNextLine()) {
            throw new Exception("EO");
        }
        String[] line = scanner.nextLine().split("=");
        if (line.length != 2) {
            throw new Exception("EO");
        }
        if (!line[0].equals(name)) {
            throw new Exception("EO");
        }
        if (!line[1].startsWith("[") || !line[1].endsWith("]")) {
            throw new Exception("EO");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < line[1].length() - 1; i++) sb.append(line[1].charAt(i));
        line[1] = sb.toString();
        String[] arguments = line[1].split(",");
        ArrayList<String> result = new ArrayList<>();
        for (String state : arguments) {
            if (name.equals("trans")) {
                result.add(state);
                continue;
            }
            try {
                Integer.parseInt(state);
                result.add(state);
                continue;
            } catch (NumberFormatException ignored) {
            }
            for (char x : state.toCharArray()) {
                if (name.equals("alpha")) {
                    if (!(isAlpha(x) || isNumber(x) || x == '_')) {
                        throw new Exception("E0");
                    }
                } else {
                    if (!(isAlpha(x) || isNumber(x))) {
                        throw new Exception("E0");
                    }
                }
            }
            result.add(state);
        }

        return result;
    }

    private boolean isAlpha(char x) {
        return ('a' <= x && x <= 'z') || ('A' <= x && x <= 'Z');
    }

    private boolean isNumber(char x) {
        return '0' <= x && x <= '9';
    }
}

/**
 * Solution for the task 1
 *
 * @author Ilya Siluyanov
 * @version 1.0
 * @since 2021-04-21
 */
interface Graph<V, E> {
    /**
     * adds a vertex to the graph
     *
     * @param value - value corresponding to a new vertex
     * @return reference to vertex with the corresponding value
     */
    Graph.Vertex<V> addVertex(V value);

    /**
     * removes the vertex v references to
     *
     * @param v - reference to the vertex to remove
     */
    void removeVertex(Graph.Vertex<V> v);

    /**
     * adds an edge to the graph. Here edge is an ordered pair {from, to, w}
     *
     * @param from   - vertex from which the edge goes outside
     * @param to     - vertex to which the edge goes to
     * @param weight - weight of the edge
     * @return - reference to corresponding edge
     */
    Graph.Edge<V, E> addEdge(Graph.Vertex<V> from, Graph.Vertex<V> to, E weight);

    /**
     * removes the edge e references to
     *
     * @param e - reference to edges to remove
     */
    void removeEdge(Graph.Edge<V, E> e);

    /**
     * @param v - reference to a vertex for which to return edges going outside
     * @return collection of references to edges which go outside from the vertex v references to
     */
    Collection<Edge<V, E>> edgesFrom(Graph.Vertex<V> v);

    /**
     * @param v - reference to a vertex for which to return edges going to
     * @return a collection of references to edges which go to the vertex v references to
     */
    Collection<Edge<V, E>> edgesTo(Graph.Vertex<V> v);

    /**
     * @param value - value of vertex for which to return a reference
     * @return reference to a vertex with value 'value', otherwise null
     */
    Graph.Vertex<V> findVertex(V value);

    /**
     * @param fromValue - value of vertex from which an edge goes outside
     * @param toValue   - value of vertex to which the same edge goes to
     * @return reference to the edge from vertex with value 'fromValue' to vertex with value 'toValue' if present,
     * otherwise null
     */
    Graph.Edge<V, E> findEdge(V fromValue, V toValue);

    /**
     * @param v - reference to a vertex from which an edge probably goes outside
     * @param u - reference to a vertex to which the same edge probably goes to
     * @return whether or not there exists an edge from vertex v to vertex u
     */
    boolean hasEdge(Graph.Vertex<V> v, Graph.Vertex<V> u);

    /**
     * class representing a vertex in a graph
     *
     * @param <V> type of value of vertex
     */
    class Vertex<V> {
        private final V value;

        public Vertex(V value) {
            this.value = value;
        }

        public V getValue() {
            return this.value;
        }

        /**
         * vertices are equal if they have the same value
         *
         * @param o - another object
         * @return whether or not vertices are equal
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;

            Graph.Vertex<V> vertex = (Graph.Vertex<V>) o;

            return value.equals(vertex.value);
        }

        /**
         * hashcode of a vertex === hashcode of the value corresponding to the vertex
         *
         * @return hashcode of a vertex
         */
        @Override
        public int hashCode() {
            return this.value.hashCode();
        }
    }

    /**
     * class representing an edge in a graph
     *
     * @param <V> type of value in vertices
     * @param <E> type of weights in edges
     */
    class Edge<V, E> {
        private final Graph.Vertex<V> from;
        private final Graph.Vertex<V> to;
        private final E weight;

        public Edge(Graph.Vertex<V> from, Graph.Vertex<V> to, E weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        public Graph.Vertex<V> getFrom() {
            return from;
        }

        public Graph.Vertex<V> getTo() {
            return to;
        }

        public E getWeight() {
            return weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Graph.Edge<V, E> edge = (Graph.Edge<V, E>) o;

            if (!from.equals(edge.from)) return false;
            if (!to.equals(edge.to)) return false;
            return weight.equals(edge.weight);
        }

        @Override
        public int hashCode() {
            String hashString = this.from + "" + this.to + "" + this.weight;
            return hashString.hashCode();
        }
    }
}

/**
 * Solution for the task 1
 *
 * @author Ilya Siluyanov
 * @version 1.0
 * @since 2021-04-21
 */
class AdjacencyMatrixGraph<V, E> implements Graph<V, E> {
    Graph.Edge<V, E>[][] adjMatrix;

    /**
     * indices and vertices have one-to-one mapping
     */
    HashMap<Vertex<V>, Integer> indices;
    ArrayList<Vertex<V>> vertices;

    public AdjacencyMatrixGraph() {
        int initialSize = 0;
        this.adjMatrix = (Graph.Edge<V, E>[][]) new Graph.Edge[initialSize][initialSize];
        this.vertices = new ArrayList<>();
        this.indices = new HashMap<>();
    }

    /**
     * adds a vertex to the graph
     *
     * @param value - value corresponding to a new vertex
     * @return reference to vertex with the corresponding value
     */
    @Override
    public Graph.Vertex<V> addVertex(V value) { //O(n^2)
        Graph.Vertex<V> v = this.findVertex(value);
        if (v != null)
            return v;
        v = new Vertex<>(value);
        this.indices.put(v, this.vertices.size());
        this.vertices.add(v);
        int newSize = this.vertices.size();
        Graph.Edge<V, E>[][] temp = (Graph.Edge<V, E>[][]) new Graph.Edge[newSize][newSize];
        for (int i = 0; i < this.adjMatrix.length; i++) {
            System.arraycopy(this.adjMatrix[i], 0, temp[i], 0, this.adjMatrix[i].length);
        }
        this.adjMatrix = temp;
        return v;
    }

    /**
     * removes the vertex v references to
     *
     * @param v - reference to the vertex to remove
     */
    @Override
    public void removeVertex(Graph.Vertex<V> v) { //O(n^2)
        if (v == null)
            return;
        int vertexIndex = this.indices.get(v);
        this.indices.remove(v);
        for (int i = vertexIndex + 1; i < this.vertices.size(); i++) {
            this.indices.put(this.vertices.get(i), i - 1);
            this.vertices.set(i - 1, this.vertices.get(i));
        }
        this.vertices.remove(this.vertices.size() - 1);

        int newSize = this.vertices.size();
        Graph.Edge<V, E>[][] temp = (Graph.Edge<V, E>[][]) new Graph.Edge[newSize][newSize];

        for (int i = 0; i < vertexIndex; i++) {
            for (int j = 0; j < vertexIndex; j++) {
                temp[i][j] = this.adjMatrix[i][j];
            }
            for (int j = vertexIndex; j < this.adjMatrix.length - 1; j++) {
                temp[i][j] = this.adjMatrix[i][j + 1];
            }
        }
        for (int i = vertexIndex; i < this.adjMatrix.length - 1; i++) {
            for (int j = 0; j < vertexIndex; j++) {
                temp[i][j] = this.adjMatrix[i + 1][j];
            }
            for (int j = vertexIndex; j < this.adjMatrix.length - 1; j++) {
                temp[i][j] = this.adjMatrix[i + 1][j + 1];
            }
        }
        this.adjMatrix = temp;
    }

    /**
     * adds an edge to the graph. Here edge is an ordered pair {from, to, w}
     *
     * @param from   - vertex from which the edge goes outside
     * @param to     - vertex to which the edge goes to
     * @param weight - weight of the edge
     * @return - reference to corresponding edge
     */
    @Override
    public Graph.Edge<V, E> addEdge(Graph.Vertex<V> from, Graph.Vertex<V> to, E weight) { //O(1)
        if (from == null || to == null)
            return null;
        Graph.Edge<V, E> edge = new Graph.Edge<>(from, to, weight);
        this.adjMatrix[this.indices.get(from)][this.indices.get(to)] = edge;
        return edge;
    }

    /**
     * removes the edge e references to
     *
     * @param e - reference to edges to remove
     */
    @Override
    public void removeEdge(Graph.Edge<V, E> e) { //O(1)
        if (e == null)
            return;
        this.adjMatrix[this.indices.get(e.getFrom())][this.indices.get(e.getTo())] = null;
    }

    /**
     * @param v - reference to a vertex for which to return edges going outside
     * @return collection of references to edges which go outside from the vertex v references to
     */
    @Override
    public Collection<Graph.Edge<V, E>> edgesFrom(Graph.Vertex<V> v) { //O(n)
        if (v == null)
            return new ArrayList<>();
        int index = this.indices.get(v);
        Collection<Graph.Edge<V, E>> edgesFrom = new ArrayList<>();
        Arrays.stream(this.adjMatrix[index]).filter(Objects::nonNull).forEach(edgesFrom::add);
        return edgesFrom;
    }

    /**
     * @param v - reference to a vertex for which to return edges going to
     * @return a collection of references to edges which go to the vertex v references to
     */
    @Override
    public Collection<Graph.Edge<V, E>> edgesTo(Graph.Vertex<V> v) {//O(n)
        if (v == null)
            return new ArrayList<>();
        int index = this.indices.get(v);
        Collection<Graph.Edge<V, E>> edgesTo = new ArrayList<>();
        for (Graph.Edge<V, E>[] row : this.adjMatrix) {
            if (row[index] != null)
                edgesTo.add(row[index]);
        }
        return edgesTo;
    }

    /**
     * @param value - value of vertex for which to return a reference
     * @return reference to a vertex with value 'value', otherwise null
     */
    @Override
    public Graph.Vertex<V> findVertex(V value) {
        for (Graph.Vertex<V> v : this.vertices)
            if (v.getValue().equals(value))
                return v;
        return null;
    }

    /**
     * @param fromValue - value of vertex from which an edge goes outside
     * @param toValue   - value of vertex to which the same edge goes to
     * @return reference to the edge from vertex with value 'fromValue' to vertex with value 'toValue' if present,
     * otherwise null
     */
    @Override
    public Graph.Edge<V, E> findEdge(V fromValue, V toValue) {
        Graph.Vertex<V> from = this.findVertex(fromValue);
        Graph.Vertex<V> to = this.findVertex(toValue);
        if (from == null || to == null)
            return null;
        return this.adjMatrix[this.indices.get(from)][this.indices.get(to)];
    }

    /**
     * detects whether or not the graph have an edge from vertex 'from' to 'to'
     *
     * @param from - reference to a vertex from which there is probably an edge going outside to 'to'
     * @param to   - reference to a vertex to for  there is probably an edge going to from 'from'
     * @return whether or there is such an edge
     */
    @Override
    public boolean hasEdge(Graph.Vertex<V> from, Graph.Vertex<V> to) {
        if (from == null || to == null)
            return false;
        return this.adjMatrix[this.indices.get(from)][this.indices.get(to)] != null;
    }

    public int getIndex(V value) {
        return this.indices.get(findVertex(value));
    }

    public Vertex<V> getVertex(int index) {
        return this.vertices.get(index);
    }

}
