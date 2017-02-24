/* 
 * Copyright (C) 2017 Laurens Weyn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package language.deconjugator;

import language.dictionary.DefTag;
import language.dictionary.Definition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds a possible valid conjugation if a word exists with the valid tags
 * @author Laurens Weyn
 */
public class ValidWord
{
    private String word, originalWord;
    private Set<DefTag> neededTags, impliedTags;
    private String process;

    public ValidWord(String originalWord, String word, Set<DefTag> neededTags, Set<DefTag> impliedTags, String process)
    {
        this.originalWord = originalWord;
        this.word = word;
        this.neededTags = neededTags;
        this.impliedTags = impliedTags;
        this.process = process.trim();
    }
    public ValidWord(String word, Set<DefTag> neededTags, Set<DefTag> impliedTags, String process)
    {
        this.originalWord = word;
        this.word = word;
        this.neededTags = neededTags;
        this.impliedTags = impliedTags;
        this.process = process.trim();
    }
    public ValidWord(String word, DefTag neededTag, String process)
    {
        this.originalWord = word;
        this.word = word;
        neededTags = new HashSet<>();
        impliedTags = new HashSet<>();
        this.process = process.trim();
        if(neededTag != null)neededTags.add(neededTag);
    }
    public ValidWord(String word, String process)
    {
        this.originalWord = word;
        this.word = word;
        neededTags = new HashSet<>();
        impliedTags = new HashSet<>();
        this.process = process;
    }
    public String getOriginalWord()
    {
        return originalWord;
    }
    public String getWord()
    {
        return word;
    }

    public Set<DefTag> getNeededTags()
    {
        return neededTags;
    }

    public Set<DefTag> getImpliedTags()
    {
        return impliedTags;
    }

    /**
     * Checks if a definition is valid for this word, assuming this matches one of the spellings
     * @param def the definition to check
     * @return true if this is a valid definition, false otherwise
     */
    public boolean defMatches(Definition def)
    {

        if(def.getTags() == null && getNeededTags().isEmpty())return true;//still accept if no tags needed
        else if (def.getTags() == null)return false;//does not have needed tags

        for(DefTag needed:getNeededTags())
        {
            if(!def.getTags().contains(needed) && !getImpliedTags().contains(needed) && !needed.toString().equals(""))
            {
                /*
                System.out.println("Tag mismatch");
                System.out.println("Definition: " + def.getFurigana());
                System.out.println("Inherent: " + def.getTags());
                System.out.println("Implied: " + getImpliedTags());
                System.out.println("Needed: " + needed);
                */
                return false;
            }
        }
        return true;
    }

    public String getProcess()
    {
        return process;
    }

    @Override
    public String toString()
    {
        // hide non-freestanding weak forms and remove parens from freestanding ones
        String temp = process;
        if(temp.startsWith("("))
        {
            temp = temp.replaceFirst("[(]", "");
            temp = temp.replaceFirst("[)]", "");
        }
        temp = temp.replaceAll("[(].*?[)]", "");
        return word + "―" + temp;
    }
    
    
}
