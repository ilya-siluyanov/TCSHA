import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    Scanner scanner;
    ArrayList<String> states;
    ArrayList<String> alphabet;
    String initialState;
    ArrayList<String> acceptingStates;
    ArrayList<String> transitions;
    String eps = "eps";
    String empty_set = "{}";
    public static final String STATES = "states";
    public static final String ALPHA = "alpha";
    public static final String INITIAL = "initial";
    public static final String ACCEPTING = "accepting";
    public static final String TRANS = "trans";
    AdjacencyMatrixGraph<String, String> graph;
    HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>> R = new HashMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        System.setIn(new FileInputStream("input.txt"));
        System.setOut(new PrintStream("output.txt"));
        try {
            new Main().go();
        } catch (Exception e) {
            e.printStackTrace();
            if(e.getMessage()==null){
                e.printStackTrace(System.out);
            }
            switch (e.getMessage()) {
                case "E0":
                    System.out.print("Error:\nE0: Input file is malformed");
                    break;
                case "E1":
                    System.out.printf("Error:\nE1: A state '%s' is not in the set of states", e.getCause().getMessage());
                    break;
                case "E2":
                    System.out.print("Error:\nE2: Some states are disjoint");
                    break;
                case "E3":
                    System.out.printf("Error:\nE3: A transition '%s' is not represented in the alphabet", e.getCause().getMessage());
                    break;
                case "E4":
                    System.out.print("Error:\nE4: Initial state is not defined");
                    break;
                case "E5":
                    System.out.print("Error:\nE5: FSA is nondeterministic");
                    break;
            }
        }
    }

    private void go() throws Exception {
        this.scanner = new Scanner(System.in);
        this.states = parseLine(STATES);
        this.alphabet = parseLine(ALPHA);
        ArrayList<String> initialStates = parseLine(INITIAL);
        if (initialStates.size() != 1)
            throw new Exception("E4");
        this.initialState = initialStates.get(0);
        this.acceptingStates = parseLine(ACCEPTING);
        this.transitions = parseLine(TRANS);

        this.graph = new AdjacencyMatrixGraph<>();
        this.states.forEach(graph::addVertex);
        Exception[] e = new Exception[1];
        this.transitions.forEach(x -> {
            if (e[0]!=null)
                return;
            String[] splitted = x.split(">");
            if (splitted.length != 3) {
                e[0] = new Exception("E0");
            }
            if (!alphabet.contains(splitted[1])) {
                e[0] = new Exception("E3", new Exception(splitted[1]));
                return;
            }
            for (Graph.Edge<String, String> edge : graph.edgesFrom(graph.findVertex(splitted[0]))) {
                if (splitted[1].equals(edge.getWeight())) {
                    e[0] = new Exception("E5");
                    return;
                }
            }
            if (!states.contains(splitted[0])) {
                e[0] = new Exception("E1", new Exception(splitted[0]));
                return;
            }
            if (!states.contains(splitted[2])) {
                e[0] = new Exception("E1", new Exception(splitted[2]));
                return;
            }
            Graph.Vertex<String> from = graph.findVertex(splitted[0]);
            Graph.Vertex<String> to = graph.findVertex(splitted[2]);
            graph.addEdge(from, to, splitted[1]);

        });
        if (e[0] != null)
            throw e[0];
        if (!this.graph.isConnected()) {
            e[0] = new Exception("E2");
        }
        if (e[0] != null)
            throw e[0];
        System.out.println(KleeneAlgorithm());
    }

    private ArrayList<String> parseLine(String arrayName) throws Exception {
        if (!scanner.hasNextLine()) {
            throw new Exception("EO");
        }
        String[] line = scanner.nextLine().split("=");
        if (line.length != 2) {
            throw new Exception("EO");
        }
        if (!line[1].startsWith("[") || !line[1].endsWith("]")) {
            throw new Exception("EO");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < line[1].length() - 1; i++) sb.append(line[1].charAt(i));
        line[1] = sb.toString();
        String[] arguments = line[1].split(",");
        arguments = Arrays.stream(arguments).filter(x -> !x.isBlank()).toArray(String[]::new);
        ArrayList<String> result = new ArrayList<>();
        for (String argument : arguments) {
            if (arrayName.equals(INITIAL) || arrayName.equals(ACCEPTING)) {
                if (!this.states.contains(argument))
                    throw new Exception("E1", new Exception(argument));
            }
            if (arrayName.equals(TRANS)) {
                result.add(argument);
                continue;
            }
            try {
                Integer.parseInt(argument);
                result.add(argument);
                continue;
            } catch (NumberFormatException ignored) {
            }
            for (char x : argument.toCharArray()) {
                if (arrayName.equals(ALPHA)) {
                    if (!(isAlpha(x) || isDigit(x) || x == '_')) {
                        throw new Exception("E0");
                    }
                } else {
                    if (!(isAlpha(x) || isDigit(x))) {
                        throw new Exception("E0");
                    }
                }
            }
            result.add(argument);
        }

        return result;
    }

    private boolean isAlpha(char x) {
        return ('a' <= x && x <= 'z') || ('A' <= x && x <= 'Z');
    }

    private boolean isDigit(char x) {
        return '0' <= x && x <= '9';
    }

    private String KleeneAlgorithm() {
        initialize();
        mainLoop();
        return buildAnswer();
    }

    private void initialize() {
        for (int i = 0; i < this.graph.numberOfVertices(); i++) {
            R.put(i, new HashMap<>());
            for (int j = 0; j < this.graph.numberOfVertices(); j++) {
                R.get(i).put(j, new HashMap<>());
                R.get(i).get(j).put(-1, "");
            }
        }


        for (int i = 0; i < this.graph.numberOfVertices(); i++) {
            for (int j = 0; j < this.graph.numberOfVertices(); j++) {
                Graph.Vertex<String> from = this.graph.getVertex(i);
                Graph.Vertex<String> to = this.graph.getVertex(j);

                for (Graph.Edge<String, String> edge : this.graph.edgesFrom(from)) {
                    if (edge.getTo().equals(to)) {
                        HashMap<Integer, String> regexps = R.get(i).get(j);
                        String prefix = regexps.get(-1);
                        if (!prefix.isBlank()) prefix += "|";
                        regexps.put(-1, prefix + edge.getWeight());
                    }
                }
            }
            HashMap<Integer, String> regexps = R.get(i).get(i);
            String prefix = regexps.get(-1);
            if (!prefix.isBlank()) prefix += "|";
            regexps.put(-1, prefix + eps);
        }
    }

    private void mainLoop() {
        for (int k = 0; k < this.graph.numberOfVertices(); k++) {
            for (int i = 0; i < this.graph.numberOfVertices(); i++) {
                for (int j = 0; j < this.graph.numberOfVertices(); j++) {
                    String regexp = R.get(i).get(k).get(k - 1);
                    if (regexp.isBlank())
                        regexp = empty_set;
                    String first = "(" + regexp + ")";

                    regexp = R.get(k).get(k).get(k - 1);
                    if (regexp.isBlank())
                        regexp = empty_set;
                    String second = "(" + regexp + ")*";

                    regexp = R.get(k).get(j).get(k - 1);
                    if (regexp.isBlank())
                        regexp = empty_set;
                    String third = "(" + regexp + ")";

                    regexp = R.get(i).get(j).get(k - 1);
                    if (regexp.isBlank())
                        regexp = empty_set;
                    String fourth = "|(" + regexp + ")";
                    R.get(i).get(j).put(k, first + second + third + fourth);
                }
            }
        }
    }

    private String buildAnswer() {
        int n = this.graph.numberOfVertices() - 1;
        StringBuilder sb = new StringBuilder();
        for (String finalState : this.acceptingStates) {
            String regexp = R.get(this.graph.getIndex(initialState)).get(this.graph.getIndex(finalState)).get(n);
            if (regexp.isBlank())
                regexp = empty_set;
            if (sb.length() == 0)
                sb.append(regexp);
            else
                sb.append("|").append(regexp);
        }
        if (sb.length() == 0)
            return empty_set;
        return sb.toString();
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
    Collection<Graph.Edge<V, E>>[][] adjMatrix;

    /**
     * indices and vertices have one-to-one mapping
     */
    HashMap<Vertex<V>, Integer> indices;
    ArrayList<Vertex<V>> vertices;

    public AdjacencyMatrixGraph() {
        int initialSize = 0;
        this.adjMatrix = (ArrayList<Edge<V, E>>[][]) new ArrayList[initialSize][initialSize];
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
        Graph.Vertex<V> v = null;
        try {
            return this.findVertex(value);
        } catch (NullPointerException ignored) {
        }
        v = new Vertex<>(value);
        this.indices.put(v, this.vertices.size());
        this.vertices.add(v);
        int newSize = this.vertices.size();
        Collection<Graph.Edge<V, E>>[][] temp = (ArrayList<Edge<V, E>>[][]) new ArrayList[newSize][newSize];
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
        Collection<Graph.Edge<V, E>>[][] temp = (ArrayList<Edge<V, E>>[][]) new ArrayList[newSize][newSize];

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
        if (this.adjMatrix[this.indices.get(from)][this.indices.get(to)] == null)
            this.adjMatrix[this.indices.get(from)][this.indices.get(to)] = new ArrayList<>();
        this.adjMatrix[this.indices.get(from)][this.indices.get(to)].add(edge);
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
        this.adjMatrix[this.indices.get(e.getFrom())][this.indices.get(e.getTo())].remove(e);
    }

    /**
     * @param from - reference to a vertex for which to return edges going outside
     * @return collection of references to edges which go outside from the vertex v references to
     */
    @Override
    public Collection<Graph.Edge<V, E>> edgesFrom(Graph.Vertex<V> from) { //O(n)
        if (from == null)
            return new ArrayList<>();
        int index = this.indices.get(from);
        Collection<Graph.Edge<V, E>> edgesFrom = new ArrayList<>();
        Arrays.stream(this.adjMatrix[index]).filter(Objects::nonNull).forEach(edgesFrom::addAll);
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
        for (Collection<Graph.Edge<V, E>>[] row : this.adjMatrix) {
            if (row[index] == null)
                continue;
            for (Edge<V, E> edge : row[index]) {
                if (edge.getTo().equals(v))
                    edgesTo.add(edge);
            }
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
        throw new NullPointerException();
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
        for (Edge<V, E> edge : this.adjMatrix[this.indices.get(from)][this.indices.get(to)]) {
            if (edge.getFrom().equals(from) && edge.getTo().equals(to))
                return edge;
        }
        throw new NullPointerException();
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
        if (this.adjMatrix[this.indices.get(from)][this.indices.get(to)] != null) {
            for (Edge<V, E> edge : this.adjMatrix[this.indices.get(from)][this.indices.get(to)]) {
                if (edge.getFrom().equals(from) && edge.getTo().equals(to))
                    return true;
            }
        }
        return false;
    }

    public int getIndex(V value) {
        return this.getIndex(findVertex(value));
    }

    public int getIndex(Vertex<V> vertex) {
        return this.indices.get(vertex);
    }

    public Vertex<V> getVertex(int index) {
        return this.vertices.get(index);
    }

    public int numberOfVertices() {
        return this.vertices.size();
    }

    public boolean isConnected() {
        boolean[] used = new boolean[this.numberOfVertices()];
        dfs(0, used);
        for (int i = 0; i < used.length; i++) {
            if (!used[i])
                return false;
        }
        return true;
    }

    private void dfs(int x, boolean[] used) {
        used[x] = true;
        for (int to : this.edgesFrom(this.getVertex(x)).stream().map(t -> this.getIndex(t.getTo())).collect(Collectors.toList())) {
            if (!used[to]) {
                dfs(to, used);
            }
        }
    }
}
