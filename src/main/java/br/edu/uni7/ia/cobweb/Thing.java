package br.edu.uni7.ia.cobweb;

public class Thing implements Sample {
	
	public static final String[] PROPERTIES = { "cor", "núcleo", "calda" };

	// Classe
	public final String clazz;
	// Propriedades
	public final String color;
	public final String core;
	public final String tail;
	
	private final String[] stringData;
	
	public Thing(String[] stringData) {
		if (stringData == null || stringData.length != 4) {
			throw new IllegalArgumentException("Argumento nulo ou fora do formato");
		}
		this.stringData = stringData;
		this.clazz = stringData[0];
		this.color = stringData[1];
		this.core = stringData[2];
		this.tail = stringData[3];
	}
	
	@Override
	public String getClazz() {
		return clazz;
	}
	
	@Override
	public String[] getProperties() {
		return PROPERTIES;
	}
	
	@Override
	public String getPropertyValue(String property) {
		int propertyIndex = 0;
		for (String listedProperty : PROPERTIES) {
			if (listedProperty.equals(property)) {
				break;
			}
			// TODO tratar erro quando propriedade não está no array 
			propertyIndex++;
		}
		
		return stringData[propertyIndex + 1];
	}
	
	@Override
	public String toString() {
		return "[" + String.join(",", stringData) + "]";
	}

}
