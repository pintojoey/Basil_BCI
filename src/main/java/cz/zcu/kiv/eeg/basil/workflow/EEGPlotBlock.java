package cz.zcu.kiv.eeg.basil.workflow;

import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockExecute;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockInput;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockOutput;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockType;
import cz.zcu.kiv.WorkflowDesigner.Visualizations.PlotlyGraphs.*;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;

import java.util.ArrayList;
import java.util.List;

@BlockType(type="EEGPlot",family="Visualization")
public class EEGPlotBlock {

    @BlockInput(name = "EEGData", type = "EEGData")
    private EEGDataPackage eegData;

    @BlockExecute
    public Graph process(){
        Graph graph=new Graph();
        Layout layout=new Layout();
        layout.setTitle("EEG signal visualization");
        graph.setLayout(layout);
        List<Trace> traces=new ArrayList<>();
        int channel=0;
        for(String name:eegData.getChannelNames()){
            Trace trace = new Trace();
            trace.setName(name);
            List<Point>points=new ArrayList<>();
            double data[]=eegData.getData()[channel];
            for(int i=0;i<data.length;i++){
                Point point=new Point(new Coordinate((double)i,data[i]),"");
                points.add(point);
            }
            trace.setPoints(points);
            channel++;
        }
        graph.setTraces(traces);
        return graph;
    }

    public EEGDataPackage getEegData() {
        return eegData;
    }

    public void setEegData(EEGDataPackage eegData) {
        this.eegData = eegData;
    }
}
