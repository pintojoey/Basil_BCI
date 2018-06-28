package cz.zcu.kiv.eeg.basil.workflow;

import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockExecute;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockInput;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockType;
import cz.zcu.kiv.WorkflowDesigner.Visualizations.Table;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@BlockType(type="EEGDataTable",family = "Visualization")
public class EEGDataTableVisualizer {

    @BlockInput(name="EEGData",type = "EEGDataList")
    private EEGDataPackageList eegDataPackageList;

    @BlockExecute
    public Table proces(){
        Table table = new Table();
        List<EEGDataPackage> eegDataPackages = eegDataPackageList.getEegDataPackage();
        List<String>columnHeaders = new ArrayList<>();
        List<String>columnHeaders2 = new ArrayList<>();


        int i=1;
        List<List<String>> rows=new ArrayList<>();
        int maxRows=0;
        int minCols=0;

        for(EEGDataPackage eegDataPackage:eegDataPackages){
            columnHeaders.addAll(Arrays.asList(new String[]{String.valueOf(i++),"",""}));
            columnHeaders2.addAll(Arrays.asList(eegDataPackage.getChannelNames()));
            double data[][]=eegDataPackage.getData();
            if(data.length>0&&data[0].length>0){
                if(maxRows<data[0].length)maxRows=data[0].length;
            }
            for(int j=0;j<data[0].length;j++){
                List<String>row;
                if(rows.size()<=j){
                    row=new ArrayList<>();
                    rows.add(row);
                }
                else{
                    row=rows.get(j);
                }
                if(row.size()<minCols){
                    List<String>blanks=new ArrayList<>();
                    for(int x=0;x<minCols-row.size();x++){
                        blanks.add("");
                    }
                    row.addAll(blanks);
                }
                for(int k=0;k<eegDataPackage.getChannelNames().length;k++){
                    row.add(String.valueOf(data[k][j]));
                }
            }
            minCols+=data.length;
        }
        rows.add(0,columnHeaders2);
        table.setColumnHeaders(columnHeaders);
        table.setRows(rows);
        table.setCaption("EEG Data Visualization");
        return table;
    }

    public EEGDataPackageList getEegDataPackageList() {
        return eegDataPackageList;
    }

    public void setEegDataPackageList(EEGDataPackageList eegDataPackageList) {
        this.eegDataPackageList = eegDataPackageList;
    }
}
