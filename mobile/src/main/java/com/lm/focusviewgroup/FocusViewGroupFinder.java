package com.lm.focusviewgroup;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The algorithm used for finding the next focusable view in a given direction
 * from a view that currently has focus.
 */
public class FocusViewGroupFinder {

    private static final ThreadLocal<FocusViewGroupFinder> tlFocusFinder =
            new ThreadLocal<FocusViewGroupFinder>() {
                @Override
                protected FocusViewGroupFinder initialValue() {
                    return new FocusViewGroupFinder();
                }
            };

    /**
     * Get the focus finder for this thread.
     */
    public static FocusViewGroupFinder getInstance() {
        return tlFocusFinder.get();
    }

    private final Rect mOtherRect = new Rect();
    private final Rect mBestCandidateRect = new Rect();

    // enforce thread local access
    private FocusViewGroupFinder() {
    }

    /**
     * Find the next view to take focus in root's descendants, starting from the view
     * that currently is focused.
     *
     * @param root      Contains focused. Cannot be null.
     * @param focused   Has focus now.
     * @param direction Direction to look.
     * @return The next focusable view, or null if none exists.
     */
    public final View findNextFocus(ViewGroup root, View focused, int direction) {
        Rect focusedRect=new Rect();
        if (focused != null) {
            focused.getFocusedRect(focusedRect);
            root.offsetDescendantRectToMyCoords(focused,focusedRect);
        }else {
            // make up a rect at top left or bottom right of root
            switch (direction) {
                case View.FOCUS_RIGHT:
                case View.FOCUS_DOWN:
                    setFocusTopLeft(root, focusedRect);
                    break;
                case View.FOCUS_FORWARD:

                    setFocusTopLeft(root, focusedRect);

                    break;

                case View.FOCUS_LEFT:
                case View.FOCUS_UP:
                    setFocusBottomRight(root, focusedRect);
                    break;
                case View.FOCUS_BACKWARD:
                    setFocusBottomRight(root, focusedRect);
                    break;
            }
        }

        switch (direction) {
            case View.FOCUS_UP:
            case View.FOCUS_DOWN:
            case View.FOCUS_LEFT:
            case View.FOCUS_RIGHT:
                return findNextFocusInAbsoluteDirection(findCandidates(root), root, focused,
                        focusedRect, direction);
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }
    }

    private ArrayList<View> findCandidates(ViewGroup root){
        ArrayList<View> candidates = new ArrayList<>();
        Queue<View> queue = new LinkedList<>();
        View child;
        for (int i = 0; i < root.getChildCount(); i++) {
            child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                queue.add(root.getChildAt(i));
                if (child instanceof FocusViewGroup) {
                    candidates.add(child);
                }
            }
            while (!queue.isEmpty()) {
                ViewGroup viewGroup = (ViewGroup) queue.poll();
                for (int j = 0; j < viewGroup.getChildCount(); j++) {
                    child = viewGroup.getChildAt(j);
                    if (child instanceof ViewGroup) {
                        queue.add(child);
                    }
                    if (child instanceof FocusViewGroup) {
                        candidates.add(child);
                    }
                }
            }
        }
        return candidates;
    }
    private void setFocusBottomRight(ViewGroup root, Rect focusedRect) {
        final int rootBottom = root.getScrollY() + root.getHeight();
        final int rootRight = root.getScrollX() + root.getWidth();
        focusedRect.set(rootRight, rootBottom, rootRight, rootBottom);
    }

    private void setFocusTopLeft(ViewGroup root, Rect focusedRect) {
        final int rootTop = root.getScrollY();
        final int rootLeft = root.getScrollX();
        focusedRect.set(rootLeft, rootTop, rootLeft, rootTop);
    }

    View findNextFocusInAbsoluteDirection(ArrayList<View> focusables, ViewGroup root, View focused,
                                          Rect focusedRect, int direction) {
        // initialize the best candidate to something impossible
        // (so the first plausible view will become the best choice)
        mBestCandidateRect.set(focusedRect);
        switch (direction) {
            case View.FOCUS_LEFT:
                mBestCandidateRect.offset(focusedRect.width() + 1, 0);
                break;
            case View.FOCUS_RIGHT:
                mBestCandidateRect.offset(-(focusedRect.width() + 1), 0);
                break;
            case View.FOCUS_UP:
                mBestCandidateRect.offset(0, focusedRect.height() + 1);
                break;
            case View.FOCUS_DOWN:
                mBestCandidateRect.offset(0, -(focusedRect.height() + 1));
        }

        View closest = null;

        int numFocusables = focusables.size();
        for (int i = 0; i < numFocusables; i++) {
            View focusable = focusables.get(i);

            // only interested in other non-root views
            if (focusable == focused || focusable == root) continue;

            // get focus bounds of other view in same coordinate system
            focusable.getFocusedRect(mOtherRect);
            root.offsetDescendantRectToMyCoords(focusable, mOtherRect);

            if (isBetterCandidate(direction, focusedRect, mOtherRect, mBestCandidateRect)) {
                mBestCandidateRect.set(mOtherRect);
                closest = focusable;
            }
        }
        return closest;
    }

    /**
     * Is rect1 a better candidate than rect2 for a focus search in a particular
     * direction from a source rect?  This is the core routine that determines
     * the order of focus searching.
     *
     * @param direction the direction (up, down, left, right)
     * @param source    The source we are searching from
     * @param rect1     The candidate rectangle
     * @param rect2     The current best candidate.
     * @return Whether the candidate is the new best.
     */
    boolean isBetterCandidate(int direction, Rect source, Rect rect1, Rect rect2) {

        // to be a better candidate, need to at least be a candidate in the first
        // place :)
        if (!isCandidate(source, rect1, direction)) {
            return false;
        }

        // we know that rect1 is a candidate.. if rect2 is not a candidate,
        // rect1 is better
        if (!isCandidate(source, rect2, direction)) {
            return true;
        }

        // if rect1 is better by beam, it wins
        if (beamBeats(direction, source, rect1, rect2)) {
            return true;
        }

        // if rect2 is better, then rect1 cant' be :)
        if (beamBeats(direction, source, rect2, rect1)) {
            return false;
        }

        // otherwise, do fudge-tastic comparison of the major and minor axis
        return (getWeightedDistanceFor(
                majorAxisDistance(direction, source, rect1),
                minorAxisDistance(direction, source, rect1))
                < getWeightedDistanceFor(
                majorAxisDistance(direction, source, rect2),
                minorAxisDistance(direction, source, rect2)));
    }

    /**
     * One rectangle may be another candidate than another by virtue of being
     * exclusively in the beam of the source rect.
     *
     * @return Whether rect1 is a better candidate than rect2 by virtue of it being in src's
     * beam
     */
    boolean beamBeats(int direction, Rect source, Rect rect1, Rect rect2) {
        final boolean rect1InSrcBeam = beamsOverlap(direction, source, rect1);
        final boolean rect2InSrcBeam = beamsOverlap(direction, source, rect2);

        // if rect1 isn't exclusively in the src beam, it doesn't win
        if (rect2InSrcBeam || !rect1InSrcBeam) {
            return false;
        }

        // we know rect1 is in the beam, and rect2 is not

        // if rect1 is to the direction of, and rect2 is not, rect1 wins.
        // for example, for direction left, if rect1 is to the left of the source
        // and rect2 is below, then we always prefer the in beam rect1, since rect2
        // could be reached by going down.
        if (!isToDirectionOf(direction, source, rect2)) {
            return true;
        }

        // for horizontal directions, being exclusively in beam always wins
        if ((direction == View.FOCUS_LEFT || direction == View.FOCUS_RIGHT)) {
            return true;
        }

        // for vertical directions, beams only beat up to a point:
        // now, as long as rect2 isn't completely closer, rect1 wins
        // e.g for direction down, completely closer means for rect2's top
        // edge to be closer to the source's top edge than rect1's bottom edge.
        return (majorAxisDistance(direction, source, rect1)
                < majorAxisDistanceToFarEdge(direction, source, rect2));
    }

    /**
     * Fudge-factor opportunity: how to calculate distance given major and minor
     * axis distances.  Warning: this fudge factor is finely tuned, be sure to
     * run all focus tests if you dare tweak it.
     */
    int getWeightedDistanceFor(int majorAxisDistance, int minorAxisDistance) {
        return 13 * majorAxisDistance * majorAxisDistance
                + minorAxisDistance * minorAxisDistance;
    }

    /**
     * Is destRect a candidate for the next focus given the direction?  This
     * checks whether the dest is at least partially to the direction of (e.g left of)
     * from source.
     * <p>
     * Includes an edge case for an empty rect (which is used in some cases when
     * searching from a point on the screen).
     */
    boolean isCandidate(Rect srcRect, Rect destRect, int direction) {
        switch (direction) {
            case View.FOCUS_LEFT:
                return (srcRect.right > destRect.right || srcRect.left >= destRect.right)
                        && srcRect.left > destRect.left;
            case View.FOCUS_RIGHT:
                return (srcRect.left < destRect.left || srcRect.right <= destRect.left)
                        && srcRect.right < destRect.right;
            case View.FOCUS_UP:
                return (srcRect.bottom > destRect.bottom || srcRect.top >= destRect.bottom)
                        && srcRect.top > destRect.top;
            case View.FOCUS_DOWN:
                return (srcRect.top < destRect.top || srcRect.bottom <= destRect.top)
                        && srcRect.bottom < destRect.bottom;
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }


    /**
     * Do the "beams" w.r.t the given direction's axis of rect1 and rect2 overlap?
     *
     * @param direction the direction (up, down, left, right)
     * @param rect1     The first rectangle
     * @param rect2     The second rectangle
     * @return whether the beams overlap
     */
    boolean beamsOverlap(int direction, Rect rect1, Rect rect2) {
        switch (direction) {
            case View.FOCUS_LEFT:
            case View.FOCUS_RIGHT:
                return (rect2.bottom >= rect1.top) && (rect2.top <= rect1.bottom);
            case View.FOCUS_UP:
            case View.FOCUS_DOWN:
                return (rect2.right >= rect1.left) && (rect2.left <= rect1.right);
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }

    /**
     * e.g for left, is 'to left of'
     */
    boolean isToDirectionOf(int direction, Rect src, Rect dest) {
        switch (direction) {
            case View.FOCUS_LEFT:
                return src.left >= dest.right;
            case View.FOCUS_RIGHT:
                return src.right <= dest.left;
            case View.FOCUS_UP:
                return src.top >= dest.bottom;
            case View.FOCUS_DOWN:
                return src.bottom <= dest.top;
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }

    /**
     * @return The distance from the edge furthest in the given direction
     * of source to the edge nearest in the given direction of dest.  If the
     * dest is not in the direction from source, return 0.
     */
    static int majorAxisDistance(int direction, Rect source, Rect dest) {
        return Math.max(0, majorAxisDistanceRaw(direction, source, dest));
    }

    static int majorAxisDistanceRaw(int direction, Rect source, Rect dest) {
        switch (direction) {
            case View.FOCUS_LEFT:
                return source.left - dest.right;
            case View.FOCUS_RIGHT:
                return dest.left - source.right;
            case View.FOCUS_UP:
                return source.top - dest.bottom;
            case View.FOCUS_DOWN:
                return dest.top - source.bottom;
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }

    /**
     * @return The distance along the major axis w.r.t the direction from the
     * edge of source to the far edge of dest. If the
     * dest is not in the direction from source, return 1 (to break ties with
     * {@link #majorAxisDistance}).
     */
    static int majorAxisDistanceToFarEdge(int direction, Rect source, Rect dest) {
        return Math.max(1, majorAxisDistanceToFarEdgeRaw(direction, source, dest));
    }

    static int majorAxisDistanceToFarEdgeRaw(int direction, Rect source, Rect dest) {
        switch (direction) {
            case View.FOCUS_LEFT:
                return source.left - dest.left;
            case View.FOCUS_RIGHT:
                return dest.right - source.right;
            case View.FOCUS_UP:
                return source.top - dest.top;
            case View.FOCUS_DOWN:
                return dest.bottom - source.bottom;
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }

    /**
     * Find the distance on the minor axis w.r.t the direction to the nearest
     * edge of the destination rectangle.
     *
     * @param direction the direction (up, down, left, right)
     * @param source    The source rect.
     * @param dest      The destination rect.
     * @return The distance.
     */
    static int minorAxisDistance(int direction, Rect source, Rect dest) {
        switch (direction) {
            case View.FOCUS_LEFT:
            case View.FOCUS_RIGHT:
                // the distance between the center verticals
                return Math.abs(
                        ((source.top + source.height() / 2) -
                                ((dest.top + dest.height() / 2))));
            case View.FOCUS_UP:
            case View.FOCUS_DOWN:
                // the distance between the center horizontals
                return Math.abs(
                        ((source.left + source.width() / 2) -
                                ((dest.left + dest.width() / 2))));
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }
}
