package org.verapdf.features.pb.objects;

import org.apache.pdfbox.pdmodel.graphics.pattern.PDShadingPattern;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.FeaturesData;
import org.verapdf.features.FeatureObjectType;
import org.verapdf.features.IFeaturesObject;
import org.verapdf.features.tools.FeatureTreeNode;
import org.verapdf.features.tools.FeaturesCollection;

/**
 * Features object for shading pattern
 *
 * @author Maksim Bezrukov
 */
public class PBShadingPatternFeaturesObject implements IFeaturesObject {

	private static final String ID = "id";

	private PDShadingPattern shadingPattern;
	private String id;
	private String shadingChild;
	private String extGStateChild;

	/**
	 * Constructs new tilling pattern features object
	 *
	 * @param shadingPattern PDShadingPattern which represents shading pattern for feature report
	 * @param id             id of the object
	 * @param extGStateChild external graphics state id which contains in this shading pattern
	 * @param shadingChild   shading id which contains in this shading pattern
	 */
	public PBShadingPatternFeaturesObject(PDShadingPattern shadingPattern, String id, String shadingChild, String extGStateChild) {
		this.shadingPattern = shadingPattern;
		this.id = id;
		this.shadingChild = shadingChild;
		this.extGStateChild = extGStateChild;
	}

	/**
	 * @return PATTERN instance of the FeatureObjectType enumeration
	 */
	@Override
	public FeatureObjectType getType() {
		return FeatureObjectType.PATTERN;
	}

	/**
	 * Reports featurereport into collection
	 *
	 * @param collection collection for feature report
	 * @return FeatureTreeNode class which represents a root node of the constructed collection tree
	 * @throws FeatureParsingException occurs when wrong features tree node constructs
	 */
	@Override
	public FeatureTreeNode reportFeatures(FeaturesCollection collection) throws FeatureParsingException {
		if (shadingPattern != null) {
			FeatureTreeNode root = FeatureTreeNode.createRootNode("pattern");
			if (id != null) {
				root.setAttribute(ID, id);
			}
			root.setAttribute("type", "shading");

			if (shadingChild != null) {
				FeatureTreeNode shading = root.addChild("shading");
				shading.setAttribute(ID, shadingChild);
			}

			parseFloatMatrix(shadingPattern.getMatrix().getValues(), root.addChild("matrix"));

			if (extGStateChild != null) {
				FeatureTreeNode exGState = root.addChild("graphicsState");
				exGState.setAttribute(ID, extGStateChild);
			}

			collection.addNewFeatureTree(FeatureObjectType.PATTERN, root);
			return root;
		}

		return null;
	}

	/**
	 * @return null
	 */
	@Override
	public FeaturesData getData() {
		return null;
	}

	private static void parseFloatMatrix(float[][] array, FeatureTreeNode parent) throws FeatureParsingException {
		for (int i = 0; i < array.length; ++i) {
			for (int j = 0; j < array.length - 1; ++j) {
				FeatureTreeNode element = parent.addChild("element");
				element.setAttribute("row", String.valueOf(i + 1));
				element.setAttribute("column", String.valueOf(j + 1));
				element.setAttribute("value", String.valueOf(array[i][j]));
			}
		}
	}
}
