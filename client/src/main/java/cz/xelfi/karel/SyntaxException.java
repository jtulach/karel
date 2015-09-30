/**
 * Karel
 * Copyright (C) 2014-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-2.0.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.xelfi.karel;

import java.util.List;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class SyntaxException extends Exception {
    private int error;
    private CharSequence[] params;
    public SyntaxException(KarelToken t) {
    }
    public SyntaxException(int error, CharSequence... params) {
        this.error = error;
        this.params = params;
    }

    int getErrorCode() {
        return error;
    }

    void fillParams(List<String> errorParams) {
        for (CharSequence s : params) {
            errorParams.add(s.toString());
        }
    }
}
