package br.edu.uni7.ia.cobweb;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.opencsv.CSVReader;

import br.edu.uni7.ia.util.Node;

public class Application implements CreditProfileInputs {

	private String creditProfileFile = "dados.csv";
	private String creditProfileFile2 = "dados2.csv";
	private String thingsFile = "things.csv";

	private JFrame frame;
	private JFrame creditProfileFrame;
	private JPanel buttonPanel;

	private JTextArea logTextArea;

//	private Scanner scan = new Scanner(System.in);

	private Semaphore drawStepSem = new Semaphore(0);
	private Semaphore runningSem = new Semaphore(0);

	private Node<Sample> root;

	public Application() {
		init();
	}

	private void init() {
		drawStepSem.drainPermits();
		// TODO Generalizar para outros dados
		List<Sample> data = loadPerfilDeCredito(creditProfileFile);
//		List<Sample> data = loadPerfilDeCredito(creditProfileFile2);
//				List<Sample> data = loadThings();

//				HashMap<String, Double> probsMap = calcProbs(data);

		getLogTextArea().setText("");

		if (data.size() > 1) {
			Sample firstSample = data.get(0);
			root = new Node<>(firstSample, calcProbs(firstSample));

			// Inicia sempre com um nó raiz
			printSample(firstSample);
			drawNode(root);
			try {
				drawStepSem.acquireUninterruptibly();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			getCreditProfileFrame().setVisible(true);

			for (int i = 1; i < data.size(); i++) {
				Sample sample = data.get(i);
				cobweb(root, sample);

				printSample(sample);
				drawNode(root);
				try {
					drawStepSem.acquireUninterruptibly();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		runningSem.release();
		drawStepSem.drainPermits();
	}

	private void printSample(Sample firstSample) {
		getLogTextArea().append(firstSample.getClazz());
		getLogTextArea().append(", ");
		for (int i = 0; i < firstSample.getProperties().length; i++) {
			String property = firstSample.getProperties()[i];
			getLogTextArea().append(firstSample.getPropertyValue(property));
			if (i < firstSample.getProperties().length - 1) {
				getLogTextArea().append(", ");
			}
		}
		getLogTextArea().append("\n");

	}

	private JFrame getCreditProfileFrame() {
		if (creditProfileFrame == null) {
			creditProfileFrame = new CreditProfileFrame(this);
		}
		return creditProfileFrame;
	}

	private JFrame getFrame() {
		if (frame == null) {
			frame = new JFrame("Árvore Cobweb");
			frame.getContentPane().setLayout(new BorderLayout());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(1500, 768);
			frame.setVisible(true);
			frame.getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
			JScrollPane scrollPane = new JScrollPane(getLogTextArea());
			frame.getContentPane().add(scrollPane, BorderLayout.EAST);
		}

		return frame;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();

			JButton nextButton = new JButton(" Próximo ");
			nextButton.addActionListener((event) -> {
				drawStepSem.release();
			});

			JButton ffButton = new JButton(" Final ");
			ffButton.addActionListener((event) -> {
				drawStepSem.release(Integer.MAX_VALUE);
			});

			JButton restartButton = new JButton(" Reiniciar ");
			restartButton.addActionListener((event) -> {
				drawStepSem.release(Integer.MAX_VALUE);
				new Thread(() -> {
					runningSem.acquireUninterruptibly();
					init();
				}).start();
			});

			buttonPanel.add(nextButton);
			buttonPanel.add(ffButton);
			buttonPanel.add(restartButton);
		}
		return buttonPanel;
	}

	private JTextArea getLogTextArea() {
		if (logTextArea == null) {
			logTextArea = new JTextArea(5, 30);
			logTextArea.setEditable(false);
		}
		return logTextArea;
	}

	private mxGraphComponent currentGraphComponent;

	private void drawNode(Node<Sample> node) {
		mxGraph graph = getGraphfromNode(node);
		graph.getModel().endUpdate();

		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
		layout.execute(graph.getDefaultParent());
		mxGraphComponent graphComponent = new mxGraphComponent(graph);

		if (currentGraphComponent != null) {
			for (Component comp : getFrame().getContentPane().getComponents()) {
				if (comp == currentGraphComponent) {
					getFrame().getContentPane().remove(currentGraphComponent);
				}
			}
		}
		currentGraphComponent = graphComponent;

		getFrame().getContentPane().add(currentGraphComponent, BorderLayout.CENTER);
		getFrame().revalidate();
	}

	public HashMap<String, Double> calcProbs(Sample sample) {
		List<Sample> oneList = new ArrayList<>();
		oneList.add(sample);
		return calcProbs(oneList);
	}

	int recursionLevel = 0;

	private double simInsertCalcUtility(Node<Sample> node, Sample occurency) {
		Node<Sample> simChild = simulatedSample(node, occurency);
		List<Node<Sample>> simChildren = cloneNodeList(node.getChildren());

		// Trocando filho real pelo simulado na lista simulada dos filhos de nó
//		simChildren.remove(child);
		removeAlike(simChildren, node);

		simChildren.add(simChild);

		// TODO Simular quando ocorrencia esta em no filho
		return calcUtility(simChildren);
	}

	public void cobweb(Node<Sample> node, Sample occurency) {
		if (recursionLevel == 0) {
//			drawNode(node);
//			try {
////				Thread.sleep(1000);
//				System.in.read();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		recursionLevel++;
		if (node.isLeaf()) {
			Node<Sample> l1 = new Node<>(node.getData().get(0), node.getProbsMap());
			Node<Sample> l2 = new Node<>(occurency, calcProbs(occurency));

			node.addData(occurency);
			node.setProbsMap(calcProbs(node.getData()));

			node.addChild(l1);
			node.addChild(l2);
		} else {
			node.addData(occurency);
			node.setProbsMap(calcProbs(node.getData()));

			Double[] sS = new Double[5];

			Double s1 = null;
			Node<Sample> s1Node = null;
			Double s2 = null;
			Node<Sample> s2Node = null;

			if (!node.getChildren().isEmpty()) {
				for (Node<Sample> child : node.getChildren()) {
					// TODO Simular quando ocorrencia esta em no filho
					double childUtility = simInsertCalcUtility(child, occurency);

					if (s1 == null || childUtility > s1) {
						s2 = s1;
						s2Node = s1Node;
						s1 = childUtility;
						s1Node = child;
					} else if (s2 == null || childUtility > s2) {
						s2 = childUtility;
						s2Node = child;
					}
				}
			}

			// Simular quando ocorrencia esta em um nó independente
			Node<Sample> simParentNode = node.clone();
			Node<Sample> newNode = new Node<>(occurency, calcProbs(occurency));
			simParentNode.addChild(newNode);

			Double s3 = calcUtility(simParentNode.getChildren());

			// Simular s1 e s2 aglutinando
			Double s4 = null;
			if (s1Node != null && s2Node != null) {
				List<Node<Sample>> simChildrenMerge = cloneNodeList(node.getChildren());
				Node<Sample> mergedNode = merge(s1Node.clone(), s2Node.clone());
				mergedNode.addData(occurency);

				removeAlike(simChildrenMerge, s1Node);
				removeAlike(simChildrenMerge, s2Node);
//				simChildrenMerge.remove(s1Node);
//				simChildrenMerge.remove(s2Node);
				simChildrenMerge.add(mergedNode);

				mergedNode.setProbsMap(calcProbs(mergedNode.getData()));

				s4 = calcUtility(simChildrenMerge);
			}

			// Simular s1 dividindo / split
			Double s5 = null;
			if (s1Node != null && !s1Node.isLeaf()) {
				List<Node<Sample>> simChildrenSplit = cloneNodeList(node.getChildren());

				List<Node<Sample>> simChildren = cloneNodeList(s1Node.getChildren());
				simChildren.forEach(child -> child.setParent(node));
//				simChildrenSplit.remove(s1Node);
				removeAlike(simChildrenSplit, s1Node);
				simChildrenSplit.addAll(simChildren);

				s5 = calcUtility(simChildrenSplit);
			}

			sS[0] = s1;
			sS[1] = s2;
			sS[2] = s3;
			sS[3] = s4;
			sS[4] = s5;

			double bestScore = getMax(sS);

			// FIXME considerar nulos
			// s1 melhor escore
			if (s1 != null && s1 == bestScore) {
				cobweb(s1Node, occurency);

				// s3 melhor escore
			} else if (s3 != null && s3 == bestScore) {
				Node<Sample> s3Node = new Node<>(occurency, calcProbs(occurency));
				node.addChild(s3Node);

				// s4 melhor escore
			} else if (s4 != null && s4 == bestScore) {
				Node<Sample> cM = merge(s1Node, s2Node);

				// remove filhos s1Node e s2Node originais de node
				node.getChildren().remove(s1Node);
				node.getChildren().remove(s2Node);

				// define o merge como novo filho
				node.addChild(cM);

				cobweb(cM, occurency);

				// s5 melhor escore
			} else if (s5 != null && s5 == bestScore) {
				// remove s1 de node
				node.getChildren().remove(s1Node);

				// adiciona seus filhos no seu lugar - split / dividir
				node.addChildren(s1Node.getChildren());

				cobweb(node, occurency);
			} else {
				System.out.println("Algo errado não está certo - não achou nenhum escore");
			}
		}
		recursionLevel--;
	}

	public void removeAlike(List<Node<Sample>> nodeList, Node<Sample> sampleAlike) {
		Node<Sample> sampleToRemove = null;
		for (Node<Sample> sample : nodeList) {
			boolean isDataEquals = false;

			if (sample.getData().size() == sampleAlike.getData().size() && sample.getData().size() > 0) {
				if (sample.getData().get(0).getClazz().equals(sampleAlike.getData().get(0).getClazz())) {
					isDataEquals = true;
				}
			}
			if (isDataEquals == true) {
				sampleToRemove = sample;
				break;
			}
		}
		if (sampleToRemove != null) {
			nodeList.remove(sampleToRemove);
		}
	}

	public List<Node<Sample>> cloneNodeList(List<Node<Sample>> original) {
		List<Node<Sample>> clonedList = new ArrayList<>();

		for (Node<Sample> node : original) {
			clonedList.add(node.clone());
		}

		return clonedList;
	}

	public Node<Sample> simulatedSample(Node<Sample> node, Sample occurency) {
		Node<Sample> simulated = node.clone();
		simulated.addData(occurency);
		simulated.setProbsMap(calcProbs(simulated.getData()));

		return simulated;
	}

	public double getMax(Double[] sS) {
		double response = Double.NEGATIVE_INFINITY;
		for (Double value : sS) {
			if (value != null && value > response) {
				response = value;
			}
		}

		return response;
	}

	// Aglutinar
	public Node<Sample> merge(Node<Sample> node1, Node<Sample> node2) {
		Node<Sample> cM = new Node<>();

		cM.setParent(node1.getParent());

		node1.getData().forEach(sample -> cM.addData(sample));
		node2.getData().forEach(sample -> cM.addData(sample));

		if (node1.isLeaf()) {
			cM.addChild(node1);
		} else {
			node1.getChildren().forEach(child -> cM.addChild(child));
		}

		if (node2.isLeaf()) {
			cM.addChild(node2);
		} else {
			node2.getChildren().forEach(child -> cM.addChild(child));
		}

		cM.setProbsMap(calcProbs(cM.getData()));

		return cM;
	}

	public double calcUtility(List<Node<Sample>> nodes) {
		double utility = 0;

		for (Node<Sample> node : nodes) {
			utility += calcClassProbability(node) * (calcClassUtility(node) - calcClassUtility(node.getParent()));
		}

		return utility / nodes.size();
	}

	// P(c1) | P(c2) ...
	public double calcClassProbability(Node<Sample> node) {
		if (node.getParent() != null) {
			double parentTotal = node.getParent().getData().size();
			double nodeTotal = node.getData().size();

			return nodeTotal / parentTotal;
		} else {
			return 1;
		}
	}

	// Sum(P(a=v / c1)^2) | Sum(P(a=v)^2)
	public double calcClassUtility(Node<Sample> node) {
		double classUtility = 0;
		for (String key : node.getProbsMap().keySet()) {
			classUtility += Math.pow(node.getProbsMap().get(key), 2);
		}
		return classUtility;
	}

	// mock method
	public double calcUtilityMock(Node<Sample> node) {
		return Math.random();
	}

	public HashMap<String, Double> calcProbs(List<Sample> samples) {
		HashMap<String, Double> probsMap = new HashMap<String, Double>();

		for (Sample sample : samples) {
			for (String property : sample.getProperties()) {
				String value = sample.getPropertyValue(property);
				if (probsMap.containsKey(value)) {
					probsMap.put(value, probsMap.get(value) + 1.0);
				} else {
					probsMap.put(value, 1.0);
				}
			}
		}

		for (String key : probsMap.keySet()) {
			probsMap.put(key, probsMap.get(key) / samples.size());
		}

		return probsMap;
	}

	public List<Sample> loadPerfilDeCredito(String fileName) {
		List<Sample> data = new ArrayList<>();
		try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
			String[] line;
			while ((line = reader.readNext()) != null) {
				data.add(new CreditProfile(line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public List<Sample> loadThings() {
		List<Sample> data = new ArrayList<>();
		try (CSVReader reader = new CSVReader(new FileReader(thingsFile))) {
			String[] line;
			while ((line = reader.readNext()) != null) {
				data.add(new Thing(line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	private mxGraph getGraphfromNode(Node<Sample> node) {
		mxGraph g = new mxGraph();

		Object parent = g.getDefaultParent();
		int width = calcWidth(node.getDataLabels());
		mxICell pCell = (mxICell) g.insertVertex(parent, null, node.getDataLabels(), 0, 0, width, 30);

		return getGraphfromNode(g, node, pCell);
	}

	private int calcWidth(List<String> labels) {
		int width = 0;
		for (String label : labels) {
			width += 8 * label.length();
		}
		if (width < 15) {
			width = 18;
		}
		return width;
	}

	private mxGraph getGraphfromNode(mxGraph g, Node<Sample> node, mxICell pCell) {
		for (Node<Sample> child : node.getChildren()) {
			int width = calcWidth(child.getDataLabels());
			mxICell cCell = (mxICell) g.insertVertex(pCell.getParent(), null, child.getDataLabels(), 0, 0, width, 30);
			g.insertEdge(pCell.getParent(), null, "", pCell, cCell);

			if (child != null) {
				getGraphfromNode(g, child, cCell);
			}
		}

		return g;
	}

	public static void main(String[] args) {
		new Application();
	}

	@Override
	public void findCreditProfile(CreditProfile perfil) {
		// TODO Auto-generated method stub
//		return null;
		if (root == null || root.getChildren() == null || root.getChildren().isEmpty()) {

		} else {
			// Adiciona temporariamente a amostra
			root.addData(perfil);
			root.setProbsMap(calcProbs(root.getData()));
			
			int choosenChildrenIndex = 0;
			double choosenChildrenUtility = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < root.getChildren().size(); i++) {
				Node<Sample> child = root.getChildren().get(i);
				double childUtility = simInsertCalcUtility(child, perfil);
				if (childUtility > choosenChildrenUtility) {
					choosenChildrenIndex = i;
					choosenChildrenUtility = childUtility;
				}
			}
			
			// remove a amostra temporária
			root.removeData(perfil);
			root.setProbsMap(calcProbs(root.getData()));

			JOptionPane.showMessageDialog(null,
					"O filho escolhido foi o de índice " + choosenChildrenIndex + " que possui os labels "
							+ prettyPrintLabels(root.getChildren().get(choosenChildrenIndex).getDataLabels()),
					"Resultado", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private String prettyPrintLabels(ArrayList<String> labels) {
		return "[ " + String.join(", ", labels) + "]";
	}

	@Override
	public void insertCreditProfile(CreditProfile perfil) {
		cobweb(root, perfil);

		printSample(perfil);
		drawNode(root);
	}
}