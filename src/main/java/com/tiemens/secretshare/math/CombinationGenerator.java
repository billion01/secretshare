/*******************************************************************************
 * Copyright (c) 2009, 2014 Tim Tiemens.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 *
 * Contributors:
 *     Tim Tiemens - initial API and implementation
 *******************************************************************************/
package com.tiemens.secretshare.math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.tiemens.secretshare.exceptions.SecretShareException;

/**
 * Combinations - selections of some members of the set where order is disregarded.
 *    The combination of n things taken k at a time without repetition
 *
 * @author timtiemens
 *
 * @param <E>
 */
public class CombinationGenerator<E>
        implements Iterator<List<E>>,
                   Iterable<List<E>>
{
    // ==================================================
    // class static data
    // ==================================================

    // ==================================================
    // class static methods
    // ==================================================

    public static void main(String[] args)
    {
        System.out.println("fact(5)=" + factorial(5));
        System.out.println("fact(3)=" + factorial(3));
        runOne(3, Arrays.asList("1", "2", "3", "4", "5"));
        runOne(4, Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h"));

        // picking 3 from 8 is basically the same as picking 5 from 8, i.e.
        //   "picking 3 to get rid of" is basically the same as "picking 5 to keep"
        // runOne(3, Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h"));
        // runOne(5, Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h"));
    }

    public static void runOne(final int numberToPick, List<String> list) {
        CombinationGenerator<String> combos = new CombinationGenerator<String>(list, numberToPick);
        System.out.println("Total number=" + combos.getTotalNumberOfCombinations());
        for(List<String> combination : combos)
        {
            System.out.println(combos.getCurrentCombinationNumber() + ": " + combination + " {" + combos.indexesAsString + "}");
        }
    }

    // ==================================================
    // instance data
    // ==================================================

    private final List<E> list;

    // currentIndexes contains the indexes to use for the NEXT iteration
    private int[] currentIndexes;

    // indexesAsString contains either null or the CURRENT iteration's indexes
    private String indexesAsString;

    //
    private final BigInteger totalNumberOfCombinations;
    // ranges 0 to totalNumber, where "0" means "you haven't called next() yet"
    private BigInteger combinationNumber;

    // ==================================================
    // factories
    // ==================================================

    // ==================================================
    // constructors
    // ==================================================

    /**
     * @param inItems collection of items to choose from
     * @param inChoiceSize number items to choose at a time ("k")
     */
    public CombinationGenerator(final Collection<E> inItems,
                                final int inChoiceSize)
    {
        if (inChoiceSize < 1)
        {
            throw new SecretShareException("choice size cannot be less than 1:" + inChoiceSize);
        }

        if (inChoiceSize > inItems.size())
        {
            throw new SecretShareException("choice size cannot be greater than size");
        }

        List<E> ourlist = new ArrayList<E>(inItems);

        this.list = Collections.unmodifiableList(ourlist);

        this.currentIndexes = new int[inChoiceSize];
        for (int i = 0; i < inChoiceSize; i++)
        {
            this.currentIndexes[i] = i;
        }

        totalNumberOfCombinations = computeNfactdivkNkFact(this.list.size(), inChoiceSize);
        combinationNumber = BigInteger.ZERO;
    }



    /**
     * Return (n!) / ( k! * (n-k)! ).
     *
     * @param n
     * @param k
     * @return value from equation above
     */
    private static BigInteger computeNfactdivkNkFact(final int n,
                                                     final int k)
    {
        final int nminusk = n - k;

        BigInteger kfactTimesnmkfact = factorial(k).multiply(factorial(nminusk));
        BigInteger nfactorial = factorial(n);

        return nfactorial.divide(kfactTimesnmkfact);
    }



    // ==================================================
    // public methods
    // ==================================================

    public final BigInteger getCurrentCombinationNumber()
    {
        return combinationNumber;
    }

    public final BigInteger getTotalNumberOfCombinations()
    {
        return totalNumberOfCombinations;
    }

    @Override
    public Iterator<List<E>> iterator()
    {
        return this;
    }

    @Override
    public boolean hasNext()
    {
        return (currentIndexes != null);
    }

    public String getIndexesAsString()
    {
        return indexesAsString;
    }

    @Override
    public List<E> next()
    {
        if (! hasNext())
        {
            // ouch - Java's exception type design makes this a hard choice:
            //        It would be nice to throw new SecretShareException() here.
            throw new NoSuchElementException();
        }

        combinationNumber = combinationNumber.add(BigInteger.ONE);

        List<E> currentCombination = new ArrayList<E>();
        for (int i : currentIndexes)
        {
            currentCombination.add(list.get(i));
        }

        // capture before moving the indexes:
        indexesAsString = Arrays.toString(currentIndexes);

        moveIndexesToNextCombination();

        return currentCombination;
    }

    @Override
    public void remove()
    {
        // ouch again
        throw new UnsupportedOperationException();
    }

    // ==================================================
    // private methods
    // ==================================================

    private void moveIndexesToNextCombination()
    {
        for (int i = currentIndexes.length - 1, j = list.size() - 1; i >= 0; i--, j--)
        {
            if (currentIndexes[i] != j)
            {
                currentIndexes[i]++;
                for (int k = i + 1; k < currentIndexes.length; k++)
                {
                    currentIndexes[k] = currentIndexes[k-1] + 1;
                }
                return;
            }
        }
        // otherwise, we are all done:
        currentIndexes = null;
    }
//    int i = r - 1;
//    while (a[i] == (n - r + i))
//    {
//        i--;
//    }
//    a[i] = a[i] + 1;
//    for (int j = i + 1; j < r; j++)
//    {
//        a[j] = a[i] + j - i;
//    }

    private static BigInteger factorial (int n)
    {
        BigInteger ret = BigInteger.ONE;
        for (int i = n; i > 1; i--)
        {
            ret = ret.multiply(BigInteger.valueOf(i));
        }
        return ret;
    }



}
