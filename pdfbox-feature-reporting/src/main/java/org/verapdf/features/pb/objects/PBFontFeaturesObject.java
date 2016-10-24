package org.verapdf.features.pb.objects;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDCIDFont;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDFontLike;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.FeatureObjectType;
import org.verapdf.features.FeaturesData;
import org.verapdf.features.FontFeaturesData;
import org.verapdf.features.IFeaturesObject;
import org.verapdf.features.pb.tools.PBCreateNodeHelper;
import org.verapdf.features.tools.FeatureTreeNode;
import org.verapdf.features.tools.FeaturesCollection;

/**
 * Feature object for fonts
 *
 * @author Maksim Bezrukov
 */
public class PBFontFeaturesObject implements IFeaturesObject {

	private static final Logger LOGGER = Logger
			.getLogger(PBFontFeaturesObject.class);

	private static final String ID = "id";

	private PDFontLike fontLike;
	private String id;
	private Set<String> extGStateChild;
	private Set<String> colorSpaceChild;
	private Set<String> patternChild;
	private Set<String> shadingChild;
	private Set<String> xobjectChild;
	private Set<String> fontChild;
	private Set<String> propertiesChild;

	/**
	 * Constructs new font features object
	 *
	 * @param fontLike        PDFontLike which represents font for feature report
	 * @param id              id of the object
	 * @param extGStateChild  set of external graphics state id which contains in resource dictionary of this font
	 * @param colorSpaceChild set of ColorSpace id which contains in resource dictionary of this font
	 * @param patternChild    set of pattern id which contains in resource dictionary of this font
	 * @param shadingChild    set of shading id which contains in resource dictionary of this font
	 * @param xobjectChild    set of XObject id which contains in resource dictionary of this font
	 * @param fontChild       set of font id which contains in resource dictionary of this font
	 * @param propertiesChild set of properties id which contains in resource dictionary of this font
	 */
	public PBFontFeaturesObject(PDFontLike fontLike, String id, Set<String> extGStateChild,
								Set<String> colorSpaceChild, Set<String> patternChild, Set<String> shadingChild,
								Set<String> xobjectChild, Set<String> fontChild, Set<String> propertiesChild) {
		this.fontLike = fontLike;
		this.id = id;
		this.extGStateChild = extGStateChild;
		this.colorSpaceChild = colorSpaceChild;
		this.patternChild = patternChild;
		this.shadingChild = shadingChild;
		this.xobjectChild = xobjectChild;
		this.fontChild = fontChild;
		this.propertiesChild = propertiesChild;
	}

	/**
	 * @return FONT instance of the FeaturesObjectTypesEnum enumeration
	 */
	@Override
	public FeatureObjectType getType() {
		return FeatureObjectType.FONT;
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
		if (fontLike != null) {
			FeatureTreeNode root = FeatureTreeNode.createRootNode("font");
			if (id != null) {
				root.setAttribute(ID, id);
			}

			if (fontLike instanceof PDFont) {
				PDFont font = (PDFont) fontLike;
				PBCreateNodeHelper.addNotEmptyNode("type", font.getSubType(), root);

				if (!(font instanceof PDType3Font)) {
					PBCreateNodeHelper.addNotEmptyNode("baseFont", font.getName(), root);
				}

				if (font instanceof PDType0Font) {
					PBCreateNodeHelper.parseIDSet(fontChild, "descendantFont", null, root.addChild("descendantFonts"));
					parseFontDescriptior(fontLike.getFontDescriptor(), root, collection);
				} else if (font instanceof PDSimpleFont) {
					PDSimpleFont sFont = (PDSimpleFont) font;

					int fc = sFont.getCOSObject().getInt(COSName.FIRST_CHAR);
					if (fc != -1) {
						root.addChild("firstChar").setValue(String.valueOf(fc));
					}
					int lc = sFont.getCOSObject().getInt(COSName.LAST_CHAR);
					if (lc != -1) {
						root.addChild("lastChar").setValue(String.valueOf(lc));
					}

					parseWidths(sFont.getWidths(), fc, root.addChild("widths"));

					COSBase enc = sFont.getCOSObject().getDictionaryObject(COSName.ENCODING);
					if (enc instanceof COSName) {
						PBCreateNodeHelper.addNotEmptyNode("encoding", ((COSName) enc).getName(), root);
					} else if (enc instanceof COSDictionary) {
						COSBase name = ((COSDictionary) enc).getDictionaryObject(COSName.BASE_ENCODING);
						if (name instanceof COSName) {
							PBCreateNodeHelper.addNotEmptyNode("encoding", ((COSName) name).getName(), root);
						}
					}

					parseFontDescriptior(fontLike.getFontDescriptor(), root, collection);

					if (sFont instanceof PDType3Font) {
						PDType3Font type3 = (PDType3Font) sFont;

						PBCreateNodeHelper.addBoxFeature("fontBBox", type3.getFontBBox(), root);
						parseFloatMatrix(type3.getFontMatrix().getValues(), root.addChild("fontMatrix"));

						parseResources(root);
					}
				}

			} else if (fontLike instanceof PDCIDFont) {
				PDCIDFont cid = (PDCIDFont) fontLike;
				PBCreateNodeHelper.addNotEmptyNode("type", cid.getCOSObject().getNameAsString(COSName.SUBTYPE), root);
				PBCreateNodeHelper.addNotEmptyNode("baseFont", cid.getBaseFont(), root);
				COSBase dw = cid.getCOSObject().getDictionaryObject(COSName.DW);
				if (dw instanceof COSInteger) {
					root.addChild("defaultWidth").setValue(String.valueOf(((COSNumber) dw).intValue()));
				}

				if (cid.getCIDSystemInfo() != null) {
					FeatureTreeNode cidS = root.addChild("cidSystemInfo");
					PBCreateNodeHelper.addNotEmptyNode("registry", cid.getCIDSystemInfo().getRegistry(), cidS);
					PBCreateNodeHelper.addNotEmptyNode("ordering", cid.getCIDSystemInfo().getOrdering(), cidS);
					cidS.addChild("supplement").setValue(String.valueOf(cid.getCIDSystemInfo().getSupplement()));

				}
				parseFontDescriptior(fontLike.getFontDescriptor(), root, collection);
			}

			collection.addNewFeatureTree(FeatureObjectType.FONT, root);
			return root;
		}

		return null;
	}

	/**
	 * @return null if it can not get font file stream and features data of the font file and descriptor in other case.
	 */
	@Override
	public FeaturesData getData() {
		PDFontDescriptor descriptor = fontLike.getFontDescriptor();
		if (descriptor != null) {
			PDStream file = descriptor.getFontFile();
			if (file == null) {
				file = descriptor.getFontFile2();
			}
			if (file == null) {
				file = descriptor.getFontFile3();
			}
			if (file != null) {
				try {
					FontFeaturesData.Builder builder = new FontFeaturesData.Builder(file.getStream().getUnfilteredStream());

					InputStream metadata = null;
					if (file.getMetadata() != null) {
						try {
							metadata = file.getMetadata().getStream().getUnfilteredStream();
						} catch (IOException e) {
							LOGGER.debug("Can not get metadata stream for font file", e);
						}
					}
					builder.metadata(metadata);

					builder.fontName(descriptor.getFontName());
					builder.fontFamily(descriptor.getFontFamily());
					builder.fontStretch(descriptor.getFontStretch());
					COSDictionary descriptorDict = descriptor.getCOSObject();
					builder.fontWeight(getNumber(descriptorDict.getDictionaryObject(COSName.FONT_WEIGHT)));
					COSBase fl = descriptorDict.getDictionaryObject(COSName.FLAGS);
					if (fl instanceof COSInteger) {
						builder.flags(Integer.valueOf(((COSInteger) fl).intValue()));
					}
					PDRectangle rex = descriptor.getFontBoundingBox();
					if (rex != null) {
						List<Double> rect = new ArrayList<>();
						rect.add(Double.valueOf(rex.getLowerLeftX()));
						rect.add(Double.valueOf(rex.getLowerLeftY()));
						rect.add(Double.valueOf(rex.getUpperRightX()));
						rect.add(Double.valueOf(rex.getUpperRightY()));
						builder.fontBBox(rect);
					}

					builder.italicAngle(getNumber(descriptorDict.getDictionaryObject(COSName.ITALIC_ANGLE)));
					builder.ascent(getNumber(descriptorDict.getDictionaryObject(COSName.ASCENT)));
					builder.descent(getNumber(descriptorDict.getDictionaryObject(COSName.DESCENT)));
					builder.leading(getNumber(descriptorDict.getDictionaryObject(COSName.LEADING)));
					builder.capHeight(getNumber(descriptorDict.getDictionaryObject(COSName.CAP_HEIGHT)));
					builder.xHeight(getNumber(descriptorDict.getDictionaryObject(COSName.XHEIGHT)));
					builder.stemV(getNumber(descriptorDict.getDictionaryObject(COSName.STEM_V)));
					builder.stemH(getNumber(descriptorDict.getDictionaryObject(COSName.STEM_H)));
					builder.avgWidth(getNumber(descriptorDict.getDictionaryObject(COSName.AVG_WIDTH)));
					builder.maxWidth(getNumber(descriptorDict.getDictionaryObject(COSName.MAX_WIDTH)));
					builder.missingWidth(getNumber(descriptorDict.getDictionaryObject(COSName.MISSING_WIDTH)));
					builder.charSet(descriptor.getCharSet());

					return builder.build();
				} catch (IOException e) {
					LOGGER.debug("Error in obtaining features data for fonts", e);
				}
			}
		}
		return null;
	}

	private static Double getNumber(Object value) {
		return (value instanceof COSNumber) ? Double.valueOf(((COSNumber) value).doubleValue()) : null;
	}


	private static void parseFontDescriptior(PDFontDescriptor descriptor, FeatureTreeNode root, FeaturesCollection collection) throws FeatureParsingException {
		if (descriptor != null) {
			FeatureTreeNode descriptorNode = root.addChild("fontDescriptor");

			PBCreateNodeHelper.addNotEmptyNode("fontName", descriptor.getFontName(), descriptorNode);
			PBCreateNodeHelper.addNotEmptyNode("fontFamily", descriptor.getFontFamily(), descriptorNode);
			PBCreateNodeHelper.addNotEmptyNode("fontStretch", descriptor.getFontStretch(), descriptorNode);
			descriptorNode.addChild("fontWeight").setValue(String.valueOf(descriptor.getFontWeight()));
			descriptorNode.addChild("fixedPitch").setValue(String.valueOf(descriptor.isFixedPitch()));
			descriptorNode.addChild("serif").setValue(String.valueOf(descriptor.isSerif()));
			descriptorNode.addChild("symbolic").setValue(String.valueOf(descriptor.isSymbolic()));
			descriptorNode.addChild("script").setValue(String.valueOf(descriptor.isScript()));
			descriptorNode.addChild("nonsymbolic").setValue(String.valueOf(descriptor.isNonSymbolic()));
			descriptorNode.addChild("italic").setValue(String.valueOf(descriptor.isItalic()));
			descriptorNode.addChild("allCap").setValue(String.valueOf(descriptor.isAllCap()));
			descriptorNode.addChild("smallCap").setValue(String.valueOf(descriptor.isScript()));
			descriptorNode.addChild("forceBold").setValue(String.valueOf(descriptor.isForceBold()));
			PBCreateNodeHelper.addBoxFeature("fontBBox", descriptor.getFontBoundingBox(), descriptorNode);

			descriptorNode.addChild("italicAngle").setValue(String.valueOf(descriptor.getItalicAngle()));
			descriptorNode.addChild("ascent").setValue(String.valueOf(descriptor.getAscent()));
			descriptorNode.addChild("descent").setValue(String.valueOf(descriptor.getDescent()));
			descriptorNode.addChild("leading").setValue(String.valueOf(descriptor.getLeading()));
			descriptorNode.addChild("capHeight").setValue(String.valueOf(descriptor.getCapHeight()));
			descriptorNode.addChild("xHeight").setValue(String.valueOf(descriptor.getXHeight()));
			descriptorNode.addChild("stemV").setValue(String.valueOf(descriptor.getStemV()));
			descriptorNode.addChild("stemH").setValue(String.valueOf(descriptor.getStemH()));
			descriptorNode.addChild("averageWidth").setValue(String.valueOf(descriptor.getAverageWidth()));
			descriptorNode.addChild("maxWidth").setValue(String.valueOf(descriptor.getMaxWidth()));
			descriptorNode.addChild("missingWidth").setValue(String.valueOf(descriptor.getMissingWidth()));
			PBCreateNodeHelper.addNotEmptyNode("charSet", descriptor.getCharSet(), descriptorNode);

			PDStream file = descriptor.getFontFile();
			if (file == null) {
				file = descriptor.getFontFile2();
			}
			if (file == null) {
				file = descriptor.getFontFile3();
			}

			descriptorNode.addChild("embedded").setValue(String.valueOf(file != null));
			if (file != null) {
				PBCreateNodeHelper.parseMetadata(file.getMetadata(), "embeddedFileMetadata", descriptorNode, collection);
			}
		}
	}

	private static void parseFloatMatrix(float[][] array, FeatureTreeNode parent) throws FeatureParsingException {
		for (int i = 0; i < array.length; ++i) {
			for (int j = 0; j < array.length - 1; ++j) {
				FeatureTreeNode element = parent.addChild("element");
				element.setAttribute("row", String.valueOf(i));
				element.setAttribute("column", String.valueOf(j));
				element.setAttribute("value", String.valueOf(array[i][j]));
			}
		}
	}

	private static void parseWidths(List<Integer> array, int firstChar, FeatureTreeNode parent) throws FeatureParsingException {
		int fc = firstChar == -1 ? 0 : firstChar;
		for (int i = 0; i < array.size(); ++i) {
			FeatureTreeNode element = parent.addChild("width");
			element.setValue(String.valueOf(array.get(i)));
			element.setAttribute("char", String.valueOf(i + fc));
		}
	}

	private void parseResources(FeatureTreeNode root) throws FeatureParsingException {

		if ((extGStateChild != null && !extGStateChild.isEmpty()) ||
				(colorSpaceChild != null && !colorSpaceChild.isEmpty()) ||
				(patternChild != null && !patternChild.isEmpty()) ||
				(shadingChild != null && !shadingChild.isEmpty()) ||
				(xobjectChild != null && !xobjectChild.isEmpty()) ||
				(fontChild != null && !fontChild.isEmpty()) ||
				(propertiesChild != null && !propertiesChild.isEmpty())) {
			FeatureTreeNode resources = root.addChild("resources");

			PBCreateNodeHelper.parseIDSet(extGStateChild, "graphicsState", "graphicsStates", resources);
			PBCreateNodeHelper.parseIDSet(colorSpaceChild, "colorSpace", "colorSpaces", resources);
			PBCreateNodeHelper.parseIDSet(patternChild, "pattern", "patterns", resources);
			PBCreateNodeHelper.parseIDSet(shadingChild, "shading", "shadings", resources);
			PBCreateNodeHelper.parseIDSet(xobjectChild, "xobject", "xobjects", resources);
			PBCreateNodeHelper.parseIDSet(fontChild, "font", "fonts", resources);
			PBCreateNodeHelper.parseIDSet(propertiesChild, "propertiesDict", "propertiesDicts", resources);
		}
	}
}
