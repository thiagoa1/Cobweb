package br.edu.uni7.ia.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.edu.uni7.ia.cobweb.Sample;

public class Node<T extends Sample> {

	private ArrayList<T> data = new ArrayList<>();

	HashMap<String, Double> probsMap = null;

	private ArrayList<Node<T>> children = new ArrayList<>();

	private Node<T> parent = null;

	public Node(T data, HashMap<String, Double> probsMap) {
		addData(data);
		this.probsMap = probsMap;
	}

	public Node() {
	}

	public boolean isLeaf() {
		if (children == null || children.isEmpty()) {
			return true;
		}
		return false;
	}

	public Node<T> addChild(Node<T> child) {
		child.setParent(this);
		this.children.add(child);
		return child;
	}

	public void addChildren(List<Node<T>> children) {
		children.forEach(child -> child.setParent(this));
		this.children.addAll(children);
	}

	public ArrayList<Node<T>> getChildren() {
		return children;
	}

	public ArrayList<T> getData() {
		return data;
	}

	public ArrayList<String> getDataLabels() {
		ArrayList<String> labels = new ArrayList<>();

		for (T data : getData()) {
			labels.add(data.getClazz());
		}
		return labels;
	}

	public void setData(ArrayList<T> data) {
		this.data = data;
	}

	public void addData(T data) {
		this.data.add(data);
	}
	
	public void removeData(T data) {
		this.data.remove(data);
	}

	public void setProbsMap(HashMap<String, Double> probsMap) {
		this.probsMap = probsMap;
	}

	public HashMap<String, Double> getProbsMap() {
		return probsMap;
	}

	public void setParent(Node<T> parent) {
		this.parent = parent;
	}

	public Node<T> getParent() {
		return parent;
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof Node) {
//			Node other = (Node) obj;
//			if (data.size() == other.data.size()) {
//				for (int i = 0; i < data.size(); i++) {
//					if (!data.get(i).equals(other.data.get(i))) {
//						return false;
//					}
//				}
//			} else {
//				return false;
//			}
//			if (children.size() == other.children.size()) {
//				for (int i = 0; i < children.size(); i++) {
//					if (!children.get(i).equals(other.children.get(i))) {
//						return false;
//					}
//				}
//			} else {
//				return false;
//			}
//			if (probsMap == null && other.probsMap == null) {
//				return true;
//			} else if ((probsMap == null && other.probsMap != null) || (probsMap != null && other.probsMap == null)) {
//				return false;
//			} else if (probsMap.size() != other.probsMap.size()) {
//				return false;
//			} else {
//				Set<String> keys = probsMap.keySet();
//				Set<String> otherKeys = other.probsMap.keySet();
//
//				for (String key : keys) {
//					if (!otherKeys.contains(keys)) {
//						return false;
//					} else if (!other.probsMap.get(key).equals(probsMap.get(key))) {
//						return false;
//					}
//				}
//			}
//			return true;
//		}
//		return false;
//	}

	@SuppressWarnings("unchecked")
	@Override
	public Node<T> clone() {
		Node<T> node = new Node<>();

		// Clonando filhos tb
		this.getChildren().forEach(child -> node.addChild(child.clone()));
		if (probsMap != null) {
			node.setProbsMap((HashMap<String, Double>) probsMap.clone());
		}
		node.data = new ArrayList<>(this.data);
		node.parent = this.parent;

		return node;
	}
}