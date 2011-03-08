package de.danielsenff.imageflow.controller;

import ij.ImagePlus;

import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import de.danielsenff.imageflow.gui.FormPanel;
import de.danielsenff.imageflow.models.connection.Output;
import de.danielsenff.imageflow.models.connection.OutputObjectChangeListener;
import de.danielsenff.imageflow.models.datatype.DataTypeFactory;
import de.danielsenff.imageflow.models.datatype.ImageDataType;
import de.danielsenff.imageflow.models.parameter.Parameter;
import de.danielsenff.imageflow.models.unit.UnitElement;

// TODO rename
public class ParameterWidgetController {

	
	/**
	 * Create a {@link JToolBar} with all required widgets based on 
	 * the Parameters of the given unit.
	 * @param unit
	 * @return
	 */
	public static JToolBar createToolbarFromUnit(UnitElement unit) {
		
		JToolBar dash = new JToolBar(unit.getLabel());
		
		FormPanel formPanel = new FormPanel();
		formPanel.setBackground(unit.getColor());
		Collection<Parameter> parameters = unit.getParameters();
		
		for (final Parameter parameter : parameters) {
			if (!parameter.isHidden()) {
				formPanel.add(parameter);
			}
		}
		
		dash.add(formPanel);
		return dash;
	}
	
	public static JPanel createWidgetFromUnit(UnitElement unit) {
		JPanel dash = new JPanel();
		
		FormPanel formPanel = new FormPanel();
		formPanel.setBackground(unit.getColor());
		Collection<Parameter> parameters = unit.getParameters();
		
		for (final Parameter parameter : parameters) {
			formPanel.add(parameter);
		}
		
		dash.add(formPanel);
		return dash;
	}

	public static JPanel createPreviewWidgetFromUnit(UnitElement unit) {
		JPanel dash = new JPanel();
		
		FormPanel formPanel = new FormPanel();
		formPanel.setBackground(unit.getColor());
		formPanel.add(new JLabel("Preview"));
		for (Output output : unit.getOutputs()) {
			
			if (output.getDataType() instanceof ImageDataType) {
				
				final JLabel imagePreview = new JLabel();
				imagePreview.setPreferredSize(new Dimension(200, 200));
				output.addOutputObjectListener(new OutputObjectChangeListener() {
					
					public void outputObjectChanged(Output output) {
						if (output.getDataType() instanceof ImageDataType
								&& output.getOutputObject() instanceof ImagePlus) {
							ImagePlus imageplus = (ImagePlus) output.getOutputObject();
							imagePreview.setIcon(new ImageIcon(imageplus.getImage()));
							imagePreview.setText("");
						}
						
					}
				});
				
				if (output.getOutputObject() == null) {
					imagePreview.setText("starts empty");
				}
				formPanel.add(imagePreview);
			} else if (output.getDataType() instanceof DataTypeFactory.Integer
					|| output.getDataType() instanceof DataTypeFactory.Double
					|| output.getDataType() instanceof DataTypeFactory.Number) {
				final JLabel imagePreview = new JLabel();
				output.addOutputObjectListener(new OutputObjectChangeListener() {
					
					public void outputObjectChanged(Output output) {
						if (output.getDataType() instanceof ImageDataType
								&& output.getOutputObject() instanceof ImagePlus) {
							ImagePlus imageplus = (ImagePlus) output.getOutputObject();
							imagePreview.setIcon(new ImageIcon(imageplus.getImage()));
							imagePreview.setText("");
						} else if (output.getDataType() instanceof DataTypeFactory.Number) {
							if(output.getOutputObject() instanceof Integer) {
								Integer value = (Integer) output.getOutputObject();
								imagePreview.setText("Result:"+  value);
							} else if(output.getOutputObject() instanceof Double) {
								Double value = (Double) output.getOutputObject();
								DecimalFormat decimal = new DecimalFormat("0.00");
								decimal.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
								imagePreview.setText("Result:"+  decimal.format(value));
							}
						} else if (output.getDataType() instanceof DataTypeFactory.Integer
								&& output.getOutputObject() instanceof Integer) {
							Integer value = (Integer) output.getOutputObject();
							imagePreview.setText("Result:"+  value);
						}
						
					}
				});
				
				if (output.getOutputObject() == null) {
					imagePreview.setText("No result yet");
				}
				formPanel.add(imagePreview);
				
				
			} else if (output.getDataType() instanceof DataTypeFactory.Double) {
				
			//} else if (output.getDataType() instanceof DataTypeFactory.String) {
				
			} 
		}
		
		dash.add(formPanel);
		return dash;
	}
}
