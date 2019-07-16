package com.tagtraum.perf.gcviewer.exp.impl;

import com.tagtraum.perf.gcviewer.exp.AbstractDataWriter;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * Export GC csv-file based log focusing on changes of heaps only
 * <p>
 * It uses the {@literal "TS(sec), GC-index,
 * PSY-Pre(K),PSY-Post(K),PSY-Total(K),
 * ParO-Pre(K),ParO-Post(K),ParO-Total(K),
 * Meta-Pre(K),Meta-Post(K),Meta-Total(K),
 * Used(K),Total(K),
 * Pause(ms),GC-Type,User(s),Sys(s),Real(s)"} format.
 *
 * @author <a href="mailto:patrick.zhang@amperecomputing.com">Patrick Zhang</a>
 */
public class HeapDataWriter extends AbstractDataWriter {

    public HeapDataWriter(OutputStream out) {
        super(out);
    }

    private void writeHeader() {
        out.println("TS(sec),GC-index," +
                "PSY-Pre(K),PSY-Post(K),PSY-Total(K)," +
                "ParO-Pre(K),ParO-Post(K),ParO-Total(K)," +
                "Meta-Pre(K),Meta-Post(K),Meta-Total(K)," +
                "Used(K),Total(K)," +
                "Pause(ms),GC-Type,User(s),Sys(s),Real(s)");
    }

    /**
     * Writes the model and flushes the internal PrintWriter.
     */
    public void write(GCModel model) throws IOException {
        writeHeader();

        StringBuffer sb1 = new StringBuffer(128);
        StringBuffer sb2 = new StringBuffer(128);
        StringBuffer sb3 = new StringBuffer(128);

        Iterator<AbstractGCEvent<?>> i = model.getEvents();
        while (i.hasNext()) {
            AbstractGCEvent<?> abstractGCEvent = i.next();
            // filter "application stopped" events
            if (abstractGCEvent instanceof GCEvent) {
                GCEvent e = (GCEvent) abstractGCEvent;

                out.print(e.getTimestamp());
                out.print(',');
                out.print(e.getNumber());
                out.print(',');

                Iterator<GCEvent> details = e.details();
                if (details != null) {
                    StringBuffer sb = null;
                    sb1.delete(0, sb1.length());
                    sb2.delete(0, sb2.length());
                    sb3.delete(0, sb3.length());
                    for (; details.hasNext(); ) {
                        AbstractGCEvent child = details.next();
                        AbstractGCEvent.Type t = child.getExtendedType().getType();
                        if (t == AbstractGCEvent.Type.PS_YOUNG_GEN) {
                            sb = sb1;
                        } else if (t == AbstractGCEvent.Type.PAR_OLD_GEN) {
                            sb = sb2;
                        } else if (t == AbstractGCEvent.Type.Metaspace) {
                            sb = sb3;
                        } else {
                            System.err.println("Unhandled GC Type: " + t.getName());
                        }
                        if (sb != null) {
                            sb.append(child.getPreUsed());
                            sb.append(',');
                            sb.append(child.getPostUsed());
                            sb.append(',');
                            sb.append(child.getTotal());
                            sb.append(',');
                        }
                    }
                    out.print(sb1.toString());
                    out.print(sb2.toString());
                    out.print(sb3.toString());
                }
                out.print(e.getPreUsed());
                out.print(',');
                out.print(e.getTotal());
                out.print(',');
                out.print(e.getPause() * 1000);
                out.print(',');
                out.print(e.getExtendedType() != null ?
                        e.getExtendedType().getName() :
                        AbstractGCEvent.ExtendedType.UNDEFINED);
                out.print(',');
                out.print(e.getWtUser());
                out.print(',');
                out.print(e.getWtSys());
                out.print(',');
                out.print(e.getWtReal());
                out.println();
            }
        }

        out.flush();
        out.flush();
    }

}
