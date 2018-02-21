package edu.stanford.futuredata.macrobase.distributed.analysis.summary.aplinearDistributed;

import edu.stanford.futuredata.macrobase.analysis.summary.util.qualitymetrics.GlobalRatioQualityMetric;
import edu.stanford.futuredata.macrobase.analysis.summary.util.qualitymetrics.QualityMetric;
import edu.stanford.futuredata.macrobase.analysis.summary.util.qualitymetrics.SupportQualityMetric;
import edu.stanford.futuredata.macrobase.datamodel.DataFrame;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Summarizer that works over both cube and row-based labeled ratio-based
 * outlier summarization.
 */
public class APLOutlierSummarizerDistributed extends APLSummarizerDistributed {
    private Logger log = LoggerFactory.getLogger("APLOutlierSummarizerDistributed");
    private String countColumn = null;

    public APLOutlierSummarizerDistributed(JavaSparkContext sparkContext) {
        super(sparkContext);
    }

    @Override
    public List<String> getAggregateNames() {
        return Arrays.asList("Outliers", "Count");
    }

    @Override
    public JavaPairRDD<Integer, int[]> getEncoded(List<String[]> columns, DataFrame input) {
        return encoder.encodeAttributesWithSupport(columns, minOutlierSupport, input.getDoubleColumnByName(outlierColumn), numPartitions, sparkContext);
    }

    @Override
    public double[][] getAggregateColumns(DataFrame input) {
        double[] outlierCol = input.getDoubleColumnByName(outlierColumn);
        double[] countCol = processCountCol(input, countColumn,  outlierCol.length);

        double[][] aggregateColumns = new double[2][];
        aggregateColumns[0] = outlierCol;
        aggregateColumns[1] = countCol;

        return aggregateColumns;
    }

    @Override
    public List<QualityMetric> getQualityMetricList() {
        List<QualityMetric> qualityMetricList = new ArrayList<>();
        qualityMetricList.add(
                new SupportQualityMetric(0)
        );
        qualityMetricList.add(
                new GlobalRatioQualityMetric(0, 1)
        );
        return qualityMetricList;
    }

    @Override
    public List<Double> getThresholds() {
        return Arrays.asList(minOutlierSupport, minRatioMetric);
    }

    @Override
    public double getNumberOutliers(double[][] aggregates) {
        double count = 0.0;
        double[] outlierCount = aggregates[0];
        for (int i = 0; i < outlierCount.length; i++) {
            count += outlierCount[i];
        }
        return count;
    }

    public String getCountColumn() {
        return countColumn;
    }
    public void setCountColumn(String countColumn) {
        this.countColumn = countColumn;
    }
    public double getMinRatioMetric() {
        return minRatioMetric;
    }
}