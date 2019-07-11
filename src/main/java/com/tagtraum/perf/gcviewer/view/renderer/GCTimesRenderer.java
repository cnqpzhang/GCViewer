package com.tagtraum.perf.gcviewer.view.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.tagtraum.perf.gcviewer.view.GCDetailsView;
import com.tagtraum.perf.gcviewer.view.ModelChart;
import com.tagtraum.perf.gcviewer.view.ModelChartImpl;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

import javax.swing.*;

/**
 * Renders all stop the world event pauses.
 * <p>
 * Date: Jun 2, 2005
 * Time: 3:31:21 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class GCTimesRenderer extends PolygonChartRenderer {
    public static final Paint DEFAULT_LINEPAINT = Color.GREEN;

    private final JPopupMenu popGCDetails = new JPopupMenu();
    private final GCDetailsView viewGCDetails = new GCDetailsView(popGCDetails);

    private HashMap<Integer, List<AbstractGCEvent<?>>> evtmap;
    private MouseAdapter eventHandler = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            // System.out.println("Clicked: " + e.getX() + ", " + e.getY());
            if (evtmap.containsKey(e.getX())) {
                System.out.println(evtmap.get(e.getX()).toString());
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            // System.out.println("Moved: " + e.getX() + ", " + e.getY());
            if (evtmap.containsKey(e.getX())) {
                String str = evtToString(evtmap.get(e.getX()));
                // System.out.println(str);
                popGCDetails.show(e.getComponent(), e.getX(), e.getY());
                viewGCDetails.setText(str);
                viewGCDetails.requestFocus();
            }
        }
    };

    private String evtToString(List<AbstractGCEvent<?>> evts) {
        StringBuilder ret = new StringBuilder();
        if (evts != null && evts.size() > 0) {
            for (AbstractGCEvent<?> evt : evts) {
                ret.append(evt.toString());
            }
            return "<html>" +
                    ret.toString().replaceAll("(\\[\\d+\\.\\d*\\])", "<br>$1") +
                    "</html>";
        }
        return "";
    }

    public GCTimesRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setLinePaint(DEFAULT_LINEPAINT);
        setDrawPolygon(false);
        setDrawLine(true);

        evtmap = new HashMap<>();
        addMouseMotionListener(eventHandler);
        addMouseListener(eventHandler);

        popGCDetails.add(viewGCDetails);
    }

    public Polygon computePolygon(ModelChart modelChart, GCModel model) {
        ScaledPolygon polygon = createTimeScaledPolygon();
        for (Iterator<AbstractGCEvent<?>> i = model.getStopTheWorldEvents(); i.hasNext(); ) {
            AbstractGCEvent<?> event = i.next();
            int x = polygon.getScaledXValue(event.getTimestamp() - model.getFirstPauseTimeStamp());
            int y = polygon.getScaledYValue(event.getPause());
            polygon.addPoint(x, y);

            int r = 3;
            for (int j = 0; j < 1 + r << 1; j++) {
                int x0 = x + j - r;
                List<AbstractGCEvent<?>> evts = null;
                if (evtmap.containsKey(x0)) {
                    evts = evtmap.get(x0);
                }
                if (evts == null) {
                    evts = new ArrayList<>();
                }
                evts.add(event);
                if (!evtmap.containsKey(x0)) {
                    evtmap.put(x0, evts);
                }
            }

        }
        // dummy point to make the polygon complete
        polygon.addPoint(model.getRunningTime(), 0.0d);
        return polygon;
    }
}
