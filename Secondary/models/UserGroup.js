var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var UserGroupSchema = new Schema({
	user:  Schema.Types.ObjectId,
	group: Schema.Types.ObjectId,
	created: { type: Date, default: Date.now },
});

var UserGroup = mongoose.model('usergroup', UserGroupSchema);

module.exports = UserGroup;