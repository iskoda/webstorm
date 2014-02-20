/**
 * GroupAnalyzerByGrouping.java
 *
 * Created on 23.1.2007, 14:32:59 by burgetr
 */
package org.burgetr.segm.areas;

import java.util.Iterator;
import java.util.Vector;

import org.burgetr.segm.Area;
import org.burgetr.segm.AreaNode;
import org.burgetr.segm.Config;
import org.burgetr.segm.Rectangular;

/**
 * @author burgetr
 *
 */
public class GroupAnalyzerByGrouping extends GroupAnalyzer
{

    public GroupAnalyzerByGrouping(AreaNode parent)
    {
        super(parent);
    }

    @Override
    public AreaNode findSuperArea(AreaNode sub, Vector<AreaNode> selected)
    {
        //TODO: oblast lze pohltit, pokud ji lze obt�ct alespo� ze dvou stran (?)
        //starting grid position
        Rectangular gp = new Rectangular(sub.getGridPosition());
        
        //try to expand
        Rectangular limit = new Rectangular(0, 0, grid.getWidth()-1, grid.getHeight()-1);
        expandToLimit(sub, gp, limit, true, true);
        
        //select areas inside of the area found
        selected.removeAllElements();
        Rectangular mingp = null;
        for (int i = 0; i < parent.getChildCount(); i++)
        {
            AreaNode chld = parent.getChildArea(i);
            if (gp.encloses(chld.getGridPosition()))
            {
                selected.add(chld);
                if (mingp == null)
                    mingp = new Rectangular(chld.getGridPosition());
                else
                    mingp.expandToEnclose(chld.getGridPosition());
            }
        }
        
        //create the new area
        Area area = new Area(parent.getArea().getX1() + grid.getColOfs(mingp.getX1()),
                             parent.getArea().getY1() + grid.getRowOfs(mingp.getY1()),
                             parent.getArea().getX1() + grid.getColOfs(mingp.getX2()+1) - 1,
                             parent.getArea().getY1() + grid.getRowOfs(mingp.getY2()+1) - 1);
        //area.setBorders(true, true, true, true);
        AreaNode ret = new AreaNode(area);
        ret.setGridPosition(mingp);
        return ret;
    }
    
    /**
     * Tries to expand the area in the grid to a greater rectangle in the given limits.
     * @param sub the area to be expanded
     * @param gp the initial grid position of the area. This structure is modified by the expansion.
     * @param limit the maximal size to expand to
     * @param hsep stop on horizontal separators
     * @param vsep stop on vertical separators
     */
    private void expandToLimit(AreaNode sub, Rectangular gp, Rectangular limit, boolean hsep, boolean vsep)
    {
        //System.out.println("Limit: " + sub + " to " + limit.getX1() + "," + limit.getY1() + "," + limit.getX2() + "," + limit.getY2());
        //System.out.println("HSep: " + hsep + " Vsep: " + vsep);
        //sep = true;
        if (grid.getWidth() > 0 && grid.getHeight() > 0 && !sub.isBackgroundSeparated())
        {
            boolean change = true;
            while (change)
            {
                change = false;
                int newx, newy;
                //expand bottom
                if (gp.getY2() < limit.getY2() && 
                    (!hsep || !sub.separatedDown()))
                {
                    newy = expandVertically(gp, limit, true, hsep);
                    if (newy > gp.getY2())
                    {
                        gp.setY2(newy);
                        change = true;
                    }
                }
                //expand left
                if (gp.getX1() > limit.getX1() && 
                    (!vsep || !sub.separatedLeft()))
                {
                    newx = expandHorizontally(gp, limit, false, vsep);
                    if (newx < gp.getX1())
                    {
                        gp.setX1(newx);
                        change = true;
                    }
                }
                //expand top
                if (gp.getY1() > limit.getY1() &&
                    (!hsep || !sub.separatedUp()))
                {
                    newy = expandVertically(gp, limit, false, hsep);
                    if (newy < gp.getY1())
                    {
                        gp.setY1(newy);
                        change = true;
                    }
                }
                //expand right
                if (gp.getX2() < limit.getX2() &&
                    (!vsep || !sub.separatedRight()))
                {
                    newx = expandHorizontally(gp, limit, true, vsep);
                    if (newx > gp.getX2())
                    {
                        gp.setX2(newx);
                        change = true;
                    }
                }
                
                if (Config.DEBUG_AREAS)
                {
                    dispArea(gp);
                    wait(Config.DEBUG_DELAY);
                }
                //System.out.println("  Expand: " + gp);
            }
        }
    }
   
    
    /**
     * Try to expand the area horizontally by a smallest step possible
     * @param gp the area position in the grid
     * @param limit the maximal expansion limit
     * @param down <code>true</code> meand expand down, <code>false<code> means expand up
     * @param sep stop on separators
     * @return the new vertical end of the area.
     */ 
    private int expandVertically(Rectangular gp, Rectangular limit, boolean down, boolean sep)
    {
        //System.out.println("exp: " + gp + (down?" _":" ^"));
        int na = down ? gp.getY2() : gp.getY1(); //what to return when it's not possible to expand
        int targety = down ? (gp.getY2() + 1) : (gp.getY1() - 1); 
        //find candidate boxes
        Vector<AreaNode> cands = new Vector<AreaNode>();
        int x = gp.getX1();
        while (x <= gp.getX2()) //scan everything at the target position
        {
            AreaNode cand = grid.getNodeAt(x, targety);
            if (cand == null)
                x++;
            else
            {
                cands.add(cand);
                x += cand.getGridPosition().getX2() + 1;
            }
        }
        //everything below/above empty, can safely expand
        if (cands.size() == 0)
            return targety;
        //try to align the candidate boxes
        for (Iterator<AreaNode> it = cands.iterator(); it.hasNext(); )
        {
            AreaNode cand = it.next();
            if (sep && cand.getArea() != null &&
                    ((!down && cand.getArea().hasBottomBorder()) ||
                     (down && cand.getArea().hasTopBorder()) ||
                      cand.isBackgroundSeparated()))
                return na; //separated, cannot expand
            else
            {
                Rectangular cgp = new Rectangular(cand.getGridPosition());
                if (cgp.getX1() == gp.getX1() && cgp.getX2() == gp.getX2())
                    return targety; //simple match
                else if (cgp.getX1() < gp.getX1() || cgp.getX2() > gp.getX2())
                    return na; //area overflows, cannot expand
                else //candidate is smaller, try to expand align to our width
                {
                    if (down)
                    {
                        Rectangular newlimit = new Rectangular(gp.getX1(), targety, gp.getX2(), limit.getY2());
                        expandToLimit(cand, cgp, newlimit, true, false);
                        if (cgp.getX1() == gp.getX1() && cgp.getX2() == gp.getX2())
                            return cgp.getY2(); //successfully aligned
                    }
                    else
                    {
                        Rectangular newlimit = new Rectangular(gp.getX1(), limit.getY1(), gp.getX2(), targety);
                        expandToLimit(cand, cgp, newlimit, true, false);
                        if (cgp.getX1() == gp.getX1() && cgp.getX2() == gp.getX2())
                            return cgp.getY1(); //successfully aligned
                    }
                }
            }
        }
        return na; //some candidates but none usable
    }
    
    /**
     * Try to expand the area horizontally by a smallest step possible
     * @param gp the area position in the grid
     * @param limit the maximal expansion limit
     * @param right <code>true</code> meand expand right, <code>false<code> means expand left
     * @param sep stop on separators
     * @return the new vertical end of the area.
     */ 
    private int expandHorizontally(Rectangular gp, Rectangular limit, boolean right, boolean sep)
    {
        //System.out.println("exp: " + gp + (right?" ->":" <-"));
        int na = right ? gp.getX2() : gp.getX1(); //what to return when it's not possible to expand
        int targetx = right ? (gp.getX2() + 1) : (gp.getX1() - 1); 
        //find candidate boxes
        boolean found = false;
        int y = gp.getY1();
        while (y <= gp.getY2()) //scan everything at the target position
        {
            AreaNode cand = grid.getNodeAt(targetx, y);
            if (cand != null)
            {
                found = true;
                if (sep && cand.getArea() != null &&
                        ((!right && cand.getArea().hasRightBorder()) ||
                         (right && cand.getArea().hasLeftBorder()) ||
                          cand.isBackgroundSeparated()))
                    return na; //separated, cannot expand
                else
                {
                    Rectangular cgp = new Rectangular(cand.getGridPosition());
                    if (cgp.getY1() == gp.getY1() && cgp.getY2() == gp.getY2())
                        return targetx; //simple match
                    else if (cgp.getY1() < gp.getY1() || cgp.getY2() > gp.getY2())
                        return na; //area overflows, cannot expand
                    else //candidate is smaller, try to expand align to our width
                    {
                        if (right)
                        {
                            Rectangular newlimit = new Rectangular(targetx, gp.getY1(), limit.getX2(), gp.getY2());
                            expandToLimit(cand, cgp, newlimit, false, true);
                            if (cgp.getY1() == gp.getY1() && cgp.getY2() == gp.getY2())
                                return cgp.getX2(); //successfully aligned
                        }
                        else
                        {
                            Rectangular newlimit = new Rectangular(limit.getX1(), gp.getY1(), targetx, gp.getY2());
                            expandToLimit(cand, cgp, newlimit, false, true);
                            if (cgp.getY1() == gp.getY1() && cgp.getY2() == gp.getY2())
                                return cgp.getX1(); //successfully aligned
                        }
                    }
                }
                //skip the candidate
                y += cand.getGridPosition().getY2() + 1;
            }
            else
                y++;
        }
        if (!found)
            return targetx; //everything below/above empty, can safely expand
        else
            return na; //some candidates but none usable
    }
    
}